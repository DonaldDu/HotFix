-keepclasseswithmembers class com.dhy.hotfix.HotFix {*;}
-keepclasseswithmembers class com.dhy.hotfix.HotFixApp {*;}
-keepclasseswithmembers class com.dhy.hotfix.IAppInit {*;}

-keep class androidx.multidex.* {*;}

-keep class * implements com.dhy.hotfix.IAppInit
-keepclasseswithmembers class * extends com.dhy.hotfix.HotFixApp {*;}