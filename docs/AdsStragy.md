1. Giai đoạn Splash (Khởi tạo)
   Thời lượng: 5 giây (Custom SplashScreen).

Hành động Load: 1. Bắt đầu load ads_lang_001_high. 2. Nếu ads_lang_001_high báo lỗi (No fill/Network
error), lập tức load ads_lang_001_normal.

Chính sách: SplashScreen phải có logo ứng dụng rõ ràng, không chứa bất kỳ thành phần nào giả mạo
giao diện hệ thống.

2. Giai đoạn Language Selection
   Màn Language Selection 1
   Show: Hiển thị Native Ads ads_lang_001 (Kết quả load từ màn Splash).

Policy: Quảng cáo Native phải có nhãn "Ad" hoặc "Quảng cáo" rõ ràng, không được đặt sát các nút điều
hướng dễ gây click nhầm.

Hành động Load tiếp theo: Sau khi user tương tác/vào màn, bắt đầu load tuần tự cho màn sau:
ads_lang_002_high -> (nếu fail) -> ads_lang_002_normal.

Màn Language Selection 2
Show: Hiển thị Native Ads ads_lang_002 (Kết quả load từ màn trước).

Hành động Load tiếp theo: Bắt đầu load tuần tự cho Onboarding: ads_onb_001_high -> (nếu fail) ->
ads_onb_001_normal.

3. Giai đoạn Onboarding
   Màn Onboarding 1
   Show: Hiển thị Native Ads ads_onb_001.

Hành động Load tiếp theo: Bắt đầu load tuần tự: ads_onb_002_high -> (nếu fail) ->
ads_onb_002_normal.

Màn Onboarding 2
Show: Hiển thị Native Ads ads_onb_002.

Hành động chuyển tiếp: Khi kết thúc Onboarding 2, chuyển sang màn Prepare Data.

4. Giai đoạn Prepare Data (Chốt chặn Interstitial)
   Thời lượng: 5 giây (Timeout).

Hành động Load:

Bắt đầu load Interstitial: ads_inter_001_high.

Nếu fail, load ngay ads_inter_001_normal.

Logic Hiển thị (Policy Compliance):

Trường hợp 1: Ads load xong trong vòng 5s -> Hiển thị Interstitial ngay. Sau khi User đóng Ads,
chuyển vào màn Home.

Trường hợp 2: Sau 5s Ads chưa load xong -> Tự động hủy lệnh load và chuyển thẳng vào Home (Tránh
việc User đang ở Home đột ngột bị hiện quảng cáo chèn ép – vi phạm trải nghiệm người dùng).