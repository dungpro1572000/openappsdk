trước tiên tôi muốn tạo 1 file config cho phép developer có thể điền cấu hình vào ví dụ như id ads,
content của màn

ví dụ như tôi muốn sử dụng OpenAppConfig để cấu hình, nó sẽ có 1 vài trường như sau:

splashConfig(
idBanner:String, idInter:String, totalDelay=20s, content:@Composable (()->Unit)? = null (nếu null thì dùng default của SDK này))

LanguageConfig( backgoundColor:Color, textColor:Color, onLanguageSelected:(String)->Unit))

OnboardingConfig(onboardingContent1:@Composable (()->Unit)? = null,onboardingContent2:@Composable (()->Unit)? = null,onboardingContent3:@Composable (()->Unit)? = null))

PrepareDataConfig(prepareDataContent:@Composable (()->Unit)? = null, onNextToMainScreen:()->Unit))

nhớ có thêm 1 hàm navigateToMainScreen() để cho phép navigate to main luôn nếu là old/new user
tất cả những content Composable = null thì dùng của mặc định luôn.

Flow như này.
New user thì splash -> language -> onboarding -> prepareData -> main

old user thì splash -> mainScreen luôn

Spec:
trong OpenAppConfig nên có hàm init để khởi tạo 1 số thứ, tôi sẽ viết sau. init này nên đặt trong application để khởi tạo

trong Splash thì load 2 loại ads là banner và interstital (spl_banner, spl_inter), nếu là new user thì sẽ load ad native onb1_native , Sẽ cho 30s để timeout:
trong thời gian timeout mà load dudowjc cả 2 loại ads thì hiển thị luôn ads banner, nếu ads banner impression rồi tiếp tục show ads interstital 
trong trường hợp quá timeout thì cho đi tiếp, nếu là newUser thì qua Language   còn oldUser thì qua main luôn

trong màn language, sau khi user chọn xong language (ấn changed language) thì mới bắt đầu đổi ngôn ngữ. sau đó check ad spl_inter đã được load và show hay chưa, nếu chưa được show thì show, nếu chưa được load thì load lại spl_inter rồi navigate sang onboarding.

trong onboarding1 thì sẽ show onb1_native và load onb2_native, next sang onboarding2 (tại màn này sẽ load trước ad prepare_native) , onboarding2 sẽ next sang onboarding3 và show onb2_native ở đây, sau đó sẽ sang màn prepareDataScreen, tuy nhiên trước khi sang prepảeDataScreen thì check ad spl_inter đã được load và show chưa, nếu load xong mà chưa show thì show, dã show rồi thì thôi, next sang preparedata.
prepareData sẽ cho delay 5s, show prepare_native, ngoài ra thì cũng check load/show ad spl_inter, nếu có thì show, không thì next sang main.
how to implement ads:

ads banner:
@Composable
SmartBannerAd(adUnitId = "ca-app-pub-xxx/xxx")

Ads Native:
NativeAdsController.MediumNativeContainerAdView(
activity = WeakReference(activity),
adId = TEST_NATIVE_ID,
nativeLayout = com.dungz.our_ads.R.layout.native_ad_medium
)

// Preload
InterAdsController.preloadAds(
activity = WeakReference(activity),
adUnitId = "ca-app-pub-xxx/xxx"
)

// Show
InterAdsController.showAds(
activity = WeakReference(activity),
adUnitId = "ca-app-pub-xxx/xxx",
onShowFailed = { /* handle failure */ },
onShowSuccess = { /* ad dismissed */ }
)

// Preload
RewardAdsController.preloadAds(
activity = WeakReference(activity),
adUnitId = "ca-app-pub-xxx/xxx"
)

// Show
RewardAdsController.showAds(
activity = WeakReference(activity),
adUnitId = "ca-app-pub-xxx/xxx",
onUserEarn = { /* user earned reward */ },
onShowFailed = { /* handle failure */ },
onShowSuccess = { /* ad dismissed */ }
)