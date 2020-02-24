package ru.spbstu

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.classinspector.elements.ElementsClassInspector
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.specs.toTypeSpec
import ru.spbstu.matchers.Unapplier
import ru.spbstu.matchers.annotations.GenerateMatchers
import ru.spbstu.matchers.array
import ru.spbstu.wheels.firstInstance
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException

private fun remap(type: TypeName, mapping: Map<TypeVariableName, TypeName>): TypeName = when(type) {
    is TypeVariableName -> if(type in mapping) mapping[type]!! else type
    is ClassName, is Dynamic, is WildcardTypeName -> type
    is ParameterizedTypeName ->
        type.rawType.parameterizedBy(type.typeArguments.map { remap(it, mapping) })
            .copy(nullable = type.isNullable, annotations = type.annotations)
    is LambdaTypeName ->
        LambdaTypeName.get(
            receiver = type.receiver?.let { remap(it, mapping) },
            parameters = type.parameters.map { it.toBuilder(type = remap(it.type, mapping)).build() },
            returnType = type.returnType?.let { remap(it, mapping) }
        ).copy(nullable = type.isNullable, annotations = type.annotations, suspending = type.isSuspending, tags = type.tags)
}

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("ru.spbstu.matchers.annotations.GenerateMatchers")
@SupportedOptions(MatchersProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
@AutoService(Processor::class)
class MatchersProcessor : AbstractProcessor() {
    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    data class PackageName(val name: String)

    @UseExperimental(KotlinPoetMetadataPreview::class)
    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment): Boolean {
        val annotatedElements = roundEnv.getElementsAnnotatedWith(GenerateMatchers::class.java)
        if (annotatedElements.isEmpty()) return false
        val env = this.processingEnv

        Thread.currentThread().contextClassLoader = javaClass.classLoader

        val kotlinElements =
            roundEnv.getElementsAnnotatedWith(processingEnv.elementUtils.getTypeElement("kotlin.Metadata"))
                .filter { it in annotatedElements }

        val inspector = ElementsClassInspector.create(env.elementUtils, env.typeUtils)
        val classMetadata = kotlinElements.mapNotNull {
            val elem = it as? TypeElement ?: return@mapNotNull null
            elem.toTypeSpec(inspector).toBuilder().apply { tags[TypeElement::class] = elem }.build()
        }

        val genFuncs = classMetadata.map { klass ->
            val poet = klass
            val origin = klass.tag(TypeElement::class)!!
            val anno = origin.getAnnotation(GenerateMatchers::class.java)
            val props = poet.propertySpecs.filter {
                KModifier.PRIVATE !in it.modifiers && KModifier.PROTECTED !in it.modifiers
            }
            val kname = origin.asClassName()
            println(anno)
            val `package` = anno.packageName.ifBlank { kname.packageName }

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
            val unappliedType = try { anno.baseClass.asTypeName() } catch(ex: MirroredTypeException) {
                ex.typeMirror.asTypeName()
            }.takeUnless { it == Nothing::class.asTypeName()  } ?: parameterizedKName

            fun unapplierType(parameter: TypeName) =
                unapplier.parameterizedBy(unapplierTyVars + parameter)

            FunSpec
                .builder(poet.name!!)
                .apply { tags[PackageName::class] = PackageName(`package`) }
                .addTypeVariables(unapplierTyVars)
                .addTypeVariables(escapedTyVars)
                .apply {
                    for (prop in props) {
                        val param = ParameterSpec
                            .builder(
                                prop.name,
                                unapplierType(remap(prop.type, tyVarMapping)),
                                prop.modifiers.filter { it == KModifier.VARARG }
                            )
                            .defaultValue("%M()", MemberName("ru.spbstu.matchers", "any"))
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
                        val classCheck = CodeBlock.of("arg·is·%T", parameterizedKName)
                        props.map {
                            CodeBlock.of("%1N.unapply(arg.%1N,·matchResultBuilder)", it)
                        }
                            .joinToCode(" &&·")
                            .let { addStatement("%L &&·%L", classCheck, it) }
                    }
                    .endControlFlow()
                    .build()
                )
                .build()
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