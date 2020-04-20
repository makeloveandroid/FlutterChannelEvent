package com.qihoo.channel_processor

import com.google.auto.service.AutoService
import com.qihoo.annotation_api.ChannelEvent
import com.qihoo.channel_processor.Consts.KEY_MODULE_NAME
import com.qihoo.channel_processor.Consts.NAME_OF_CHANNEL
import com.qihoo.channel_processor.Consts.PACKAGE_OF_GENERATE_FILE
import com.qihoo.channel_processor.Consts.SEPARATOR
import com.squareup.javapoet.*
import javax.annotation.processing.*
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements

/**
 * 这个 AutoService 就是通过 com.google.auto.service:auto-service 自动注册一个 注解处理器
 * 它负责动态生成一些配置文件
 */
@AutoService(Processor::class)
class ChannelMethodProcessor : AbstractProcessor() {
    private var moduleName: String = ""
    private lateinit var filer: Filer
    private lateinit var elementUtils: Elements

    private lateinit var logger: Logger

    @Synchronized
    override fun init(processingEnvironment: ProcessingEnvironment) {
        super.init(processingEnvironment)
        logger = Logger(processingEnvironment.messager)
        elementUtils = processingEnv.elementUtils
        // 获取对应输出路径
        filer = processingEnvironment.filer


        // 获取modelName,获得传入的参数
        val options =
            processingEnv.options
        if (options.isNotEmpty()) {
            moduleName = options[KEY_MODULE_NAME] ?: ""
        }
        if(moduleName.isEmpty()){
            throw RuntimeException("请通过配置 CHANNEL_MODULE_NAME")
        }
    }

    /**
     * 这里处理通过 ChannelEvent 注解的类
     * 通过 javapoet 生成 Java 代码就在这里编写
     */
    override fun process(
        annotations: Set<TypeElement?>,
        roundEnvironment: RoundEnvironment
    ): Boolean {
        if (annotations.isNotEmpty()) {
            // 获取所有通过 ChannelEvent 注解的类
            val routeElements: Set<Element> =
                roundEnvironment.getElementsAnnotatedWith(
                    ChannelEvent::class.java
                )

            parseChannelEvent(routeElements)

        }

        return false
    }

    private fun parseChannelEvent(routeElements: Set<Element>) {

        // 生成的类的文件名
        val classfileName: String = NAME_OF_CHANNEL + SEPARATOR + moduleName

        // 获取 BaseChannel 类型
        val baseChannelType =
            elementUtils.getTypeElement("com.qihoo.annotation_api.base.BaseChannel")

        val rootChannelType =
            elementUtils.getTypeElement("com.qihoo.annotation_api.base.RootChannel")

        // 构建Map参数 Map<String, Class<? extends BaseChannel>>
        val baseChannelMapP = ParameterizedTypeName.get(
            ClassName.get(Map::class.java),
            ClassName.get(String::class.java),
            ParameterizedTypeName.get(
                ClassName.get(Class::class.java),
                WildcardTypeName.subtypeOf(ClassName.get(baseChannelType))
            )
        )

        // 设置参数名是
        val channelParamSpec: ParameterSpec =
            ParameterSpec.builder(baseChannelMapP, "channelEventsMap").build()


        // 为类增加一个方法 load(Map<String,BaseChannel> channelEventsMap)
        val loadMethodBuilder =
            MethodSpec.methodBuilder("load")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override::class.java)
                .addParameter(channelParamSpec)


        routeElements.forEach { element ->
            val asType = element.asType()

            // 获取注解信息
            val channelEventA = element.getAnnotation(ChannelEvent::class.java)

            // 获取方法昵称
            val methodName = channelEventA.methodName

            // 获取注解类的信息
            val className = ClassName.get(element as TypeElement)


            // 接下来将 put(methodName,class)
            loadMethodBuilder.addStatement("channelEventsMap.put(\$S,\$T.class)", methodName, className)


        }

        // 类的参数
        val classP = TypeSpec.classBuilder(classfileName)
            // 设置实现接口
            .addSuperinterface(ClassName.get(rootChannelType))
            .addModifiers(Modifier.PUBLIC).addMethod(loadMethodBuilder.build())
            .build()

        // 写出文件
        JavaFile.builder(
            PACKAGE_OF_GENERATE_FILE,
            classP
        ).build().writeTo(filer)
    }

    /**
     * 指定需要处理的解析的注解
     * 这里就处理自定义的 ChannelEvent 注解
     */
    override fun getSupportedAnnotationTypes(): Set<String> {
        return setOf(ChannelEvent::class.java.canonicalName)
    }
}
