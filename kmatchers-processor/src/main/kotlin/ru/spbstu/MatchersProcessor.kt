package ru.spbstu

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.classinspector.elements.ElementsClassInspector
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.specs.toTypeSpec
import ru.spbstu.matchers.Unapplier
import ru.spbstu.matchers.annotations.GenerateMatchers
import ru.spbstu.matchers.annotations.GenerateMultipleMatchers
import ru.spbstu.matchers.array
import ru.spbstu.wheels.firstInstance
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException

private fun remap(type: TypeName, mapping: Map<TypeVariableName, TypeName>): TypeName = when (type) {
    is TypeVariableName -> if (type in mapping) mapping[type]!! else type
    is ClassName, is Dynamic, is WildcardTypeName -> type
    is ParameterizedTypeName ->
        type.rawType.parameterizedBy(type.typeArguments.map { remap(it, mapping) })
            .copy(nullable = type.isNullable, annotations = type.annotations)
    is LambdaTypeName ->
        LambdaTypeName.get(
            receiver = type.receiver?.let { remap(it, mapping) },
            parameters = type.parameters.map { it.toBuilder(type = remap(it.type, mapping)).build() },
            returnType = type.returnType?.let { remap(it, mapping) }
        ).copy(
            nullable = type.isNullable,
            annotations = type.annotations,
            suspending = type.isSuspending,
            tags = type.tags
        )
}

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes(
    "ru.spbstu.matchers.annotations.GenerateMatchers",
    "ru.spbstu.matchers.annotations.GenerateMultipleMatchers"
)
@SupportedOptions(MatchersProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
@AutoService(Processor::class)
class MatchersProcessor : AbstractProcessor() {
    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    data class PackageName(val name: String)

    @UseExperimental(KotlinPoetMetadataPreview::class)
    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment): Boolean {
        val annotatedElements = roundEnv.getElementsAnnotatedWith(GenerateMatchers::class.java) +
                roundEnv.getElementsAnnotatedWith(GenerateMultipleMatchers::class.java)
        if (annotatedElements.isEmpty()) return false
        val env = this.processingEnv

        Thread.currentThread().contextClassLoader = javaClass.classLoader

        val kotlinElements = annotatedElements.filter { it.getAnnotation(Metadata::class.java) !== null }

        val inspector = ElementsClassInspector.create(env.elementUtils, env.typeUtils)
        val classMetadata = kotlinElements.mapNotNull {
            val elem = it as? TypeElement ?: return@mapNotNull null
            elem.toTypeSpec(inspector).toBuilder().apply { tags[TypeElement::class] = elem }.build()
        }

        val genFuncs = classMetadata.flatMap { klass ->
            val poet = klass
            val origin = klass.tag(TypeElement::class)!!
            val annos = origin.getAnnotationsByType(GenerateMatchers::class.java) +
                    origin.getAnnotation(GenerateMultipleMatchers::class.java)?.children.orEmpty()
            val props = poet.propertySpecs.filter {
                KModifier.PRIVATE !in it.modifiers && KModifier.PROTECTED !in it.modifiers
            }
            val kname = origin.asClassName()
            annos.map { anno ->
                val `package` = anno.packageName.ifBlank { kname.packageName }
                val fname = anno.functionName.ifBlank { poet.name }
                    ?: throw IllegalArgumentException("No name defined for class $origin")
                val fmodifiers = anno.functionModifiers
                val generateReceiver = "infix" in fmodifiers || "operator" in fmodifiers
                val generateDefaults = "infix" !in fmodifiers

                val unapplier = Unapplier::class.asClassName()
                val escapedTyVars = poet.typeVariables.map {
                    TypeVariableName("Re" + it.name, bounds = it.bounds)
                }
                val tyVarMapping = poet.typeVariables.zip(escapedTyVars).toMap()
                val unapplierTyVars = (1..6).map {
                    TypeVariableName("T$it")
                }
                val parameterizedKName = when {
                    escapedTyVars.isEmpty() -> kname
                    else -> kname.parameterizedBy(escapedTyVars)
                }
                val unappliedType = try {
                    anno.baseClass.asTypeName()
                } catch (ex: MirroredTypeException) {
                    ex.typeMirror.asTypeName()
                }.takeUnless { it == Nothing::class.asTypeName() } ?: parameterizedKName

                fun unapplierType(parameter: TypeName) =
                    unapplier.parameterizedBy(unapplierTyVars + parameter)

                FunSpec
                    .builder(fname)
                    .addModifiers(fmodifiers.map { KModifier.valueOf(it.toUpperCase()) })
                    .apply { tags[PackageName::class] = PackageName(`package`) }
                    .addTypeVariables(unapplierTyVars)
                    .addTypeVariables(escapedTyVars)
                    .apply {
                        val propsIt = props.iterator()
                        if(generateReceiver && propsIt.hasNext()) {
                            receiver(unapplierType(remap(propsIt.next().type, tyVarMapping)))
                        }
                        for (prop in propsIt) {
                            val param = ParameterSpec
                                .builder(
                                    prop.name,
                                    unapplierType(remap(prop.type, tyVarMapping)),
                                    prop.modifiers.filter { it == KModifier.VARARG }
                                )
                                .apply {
                                    if(generateDefaults && KModifier.VARARG !in prop.modifiers)
                                        defaultValue("%M()", MemberName("ru.spbstu.matchers", "any"))
                                }
                                .build()
                            addParameter(param)
                        }
                    }
                    .returns(unapplierType(unappliedType))
                    .addCode(CodeBlock
                        .builder()
                        .beginControlFlow(
                            "return %M·{·arg, matchResultBuilder·-> ",
                            MemberName("ru.spbstu.matchers", "unapplier")
                        )
                        .apply {
                            val propsIt = props.iterator()
                            val builder = mutableListOf<CodeBlock>()

                            builder += CodeBlock.of("arg·is·%T", parameterizedKName)
                            if(generateReceiver && propsIt.hasNext()) {
                                builder += CodeBlock.of("unapply(arg.%N,·matchResultBuilder)", propsIt.next())
                            }
                            for(prop in propsIt) {
                                builder += CodeBlock.of("%1N.unapply(arg.%1N,·matchResultBuilder)", prop)
                            }
                            addStatement("%L", builder.joinToCode(" &&·"))
                        }
                        .endControlFlow()
                        .build()
                    )
                    .build()
            }
        }

        genFuncs.groupBy { it.tag(PackageName::class)!! }.map { (p, funcs) ->
            println(p)
            FileSpec.builder(p.name, "GeneratedMatchers")
                .apply {
                    for (f in funcs) {
                        addFunction(f)
                    }
                }
                .build()
                .writeTo(processingEnv.filer)
        }

        return true
    }
}