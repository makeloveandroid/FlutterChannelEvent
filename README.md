## 故事背景
个人开发了一款 `Flutter` 项目 [奇记清单](http://118.24.60.137/)，在项目中需要用到一种 `Channel` 通讯技术，主要是完成 `Flutter` 与 `Native` 通讯过程。

![](http://p0.qhimg.com/t0110e05147691f261a.png)

![](http://p0.qhimg.com/t01df61bb48d3447a88.png)

```java
// 注册Flutter端发来数据的回调
setMessageHandler { methodAciton, re: BasicMessageChannel.Reply<String> ->
    // 1.收到Flutter端的Json数据,然后解析生成MethodChannelBean对象
    var bean = gson.fromJson(methodAciton, MethodChannelBean::class.java)
    if (bean != null) {
        // 2.这里就是主要的反射调用CoreMethodChannel的方法
        CoreMethodChannel.invoke(re, bean, this@MainActivity)
    }
}

// 反射调用逻辑
fun invoke(reply: BasicMessageChannel.Reply<String>, methodBean: MethodChannelBean, activity: Activity) {
    try {
        val method = CoreMethodChannel.javaClass.getMethod(methodBean.methodName, String::class.java, BasicMessageChannel.Reply::class.java, Activity::class.java)
        method.invoke(null, methodBean.valueStr, reply, activity)
    } catch (e: Exception) {
        print(e)
        Log.d("wyz", "方法调用错误:${e.localizedMessage}")
    }
}

```

思考下，这样做有啥问题呢？

1. 混淆问题，对应反射调用的方法不能混淆。
2. 效率，反射会生成很多的临时变量，查找对应方法也需要遍历，用多了影响性能。
3. 现在组件化越来越广泛，若这样无法完成模块解耦。

## 解决方案
我们可以通过 `APT（Annotation Processing Tool）` 和 `AOP（Aspect Oriented Programming）`，来完美解决这个问题。

![](http://p0.qhimg.com/t01990d3636563d9c3f.jpg)

### 什么是APT？
`APT`是什么呢？俗称注解处理器，我们可以在类丶字段丶方法上加对应注解，注解处理器可以在编译期期间帮我们获取到指定注解的信息，嘿嘿，然后。。。。我们就可以为所欲为啦！！
大部分情况都是获取到注解信息后，通过 `JavaPoet` 自动生成部分 `Java` 代码。

> `JavaPoet` 是什么？它是封装了生成 `Java` 代码的一些列方法，并可以输出一个 `Java` 文件。当然你要是愿意字符串拼 `Java` 代码，我也不拦着你。

### 什么是AOP？
`AOP` 是什么呢？俗称面向切面编程！什么是面向切面编程呢？对不起，概念性的东西自己 `Google`。

首先我们先简单了解下 `Android` 的 `Dex` 文件生成过程：

![](http://p0.qhimg.com/t01d2a6a9d175e362fc.png)

> 以上是个人觉得最容易理解的Dex文件生成过程，给我这种小白看的，大神觉得画错了，那就错了吧！

了解了这个，我们在回过头来说下 `AOP`。试想下，如果我们能在生成 `Dex` 文件前，对生成的 `class` 或 `Jar` 中的 `class` 做修改是不是能做到很多事情呢？比如说常见的性能监控。

那要如何如何做呢？其实 `Google` 爸爸，也为我们提供了方案！
现在我们开发 `Andorid` 都大部分都使用 `Gradle` 构建项目，并且都使用了 `Google` 提供的 `com.android.tools.build:gradle:x.x.x` 插件，这个插件就是在 `Gralde` 构建中开发了一款 `Plugin插件`，它主要负责就是打 `Andorid` 包。

此 `Plugin` 插件为我们提供给了可以注册一个 `Transform` ，它允许我们在生成 `Dex` 文件前，操作 `class` 或 `Jar` 包。

好，现在我们知道能通过 `Transform` 获取到 `class` 了，那如何改造 `class` 呢？市面上有2中方案：
1. 通过 `AspectJ` 改造（简单易学），[具体使用可以参考我儿时写的文章](https://blog.csdn.net/wenyingzhi/article/details/83989707)
2. 通过 `ASM` 库，主要是封装了解析和修改 `class` 文件的方法（难度很高，需要了解很多虚拟机调用方法的知识）。

> 当然我的方案是使用 `ASM`，谁叫我喜欢装X呢。

![](https://i02piccdn.sogoucdn.com/13b0135cfcba1b8d)

## 分析解决流程
方案有啦！工具有啦！技术。。。。算有吧！我们分析下我们要完成这个方案的过程。

![](http://p0.qhimg.com/t010de4a93697670ccd.png)

![](http://p0.qhimg.com/t01a34af7f80b78a103.jpg)

下一篇文章，我们从技术和代码角度来看这些流程就清晰很多啦！！

![](http://p0.qhimg.com/t01d0b8fcf8ac6c9ad7.gif)
