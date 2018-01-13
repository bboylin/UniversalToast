### UniversalToast：一个简洁优雅的toast组件，支持点击

---

![](./art/art.gif)

#### features
* 优雅 & 灵活
* 可点击 & 可随意设置显示时长（通过WimdowManager添加view实现）
* 主动避免android 7.0使用toast可能出现的BadTokenException

#### Usages
* step 1 : 添加依赖
```gradle
allprojects {
    repositories {
        ......
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    ......
    compile 'com.github.bboylin:UniversalToast:v1.0.2'
}
```
* step 2 : api类似原生toast
```java
UniversalToast.makeText(context, text, duration).show();
UniversalToast.makeText(context, text, duration,type).show();
```
`duration` 应该是`UniversalToast.LENGTH_LONG` 和`UniversalToast.LENGTH_SHORT`其中之一,
`type` 应该是`UniversalToast.UNIVERSAL` ,`UniversalToast.EMPHASIZE`,`UniversalToast.EMPHASIZE`三者之一,未指定则默认为`UniversalToast.UNIVERSAL`.

![](./art/universal.png)
![](./art/emphasize.png)
![](./art/clickable.png)

* 更多API:

![](./art/api.png)
```java
//example
UniversalToast.makeText(context, text, UniversalToast.LENGTH_SHORT, UniversalToast.CLICKABLE)
              .setGravity(gravity,xOffset,yOffset)
              .setBackground(drawable)//设置背景
              .setColor(R.color.my_color)//设置背景色
              .setIcon(R.drawable.my_ic)// 设置icon
              .setClickCallBack(text,R.drawable.my_btn,onClickListener) //设置点击listener
              .show();
```
有三种默认的图标提供，用`showSuccess()`,`showWarning()` ， `showError()`代替`show()`即可采用对应的图标。

![](./art/success.png)
![](./art/warning.png)
![](./art/error.png)

感谢 : [ToastCompat](https://github.com/drakeet/ToastCompat)