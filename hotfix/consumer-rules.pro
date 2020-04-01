-keepclasseswithmembers class com.dhy.hotfix.HotFix {
    public <methods>;
}
-keepclasseswithmembers class com.dhy.hotfix.IAppInit {*;}
-keep class * implements com.dhy.hotfix.IAppInit

-keep class androidx.multidex.*
-keep class android.support.multidex.*