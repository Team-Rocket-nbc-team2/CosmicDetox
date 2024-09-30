
# [Cosmic Detox] 스마트폰에서 벗어나볼까요?



# ![image](https://github.com/user-attachments/assets/381b14c1-ca31-46fc-b8c5-18f6a6098e2f)







## 1. 프로젝트 소개
  
**현대인이라면 누구나 도파민 중독?**
**코스믹 디톡스와 함께 우주여행 하며 디지털 디톡스 해보세요!**

**휴대폰을 놓지 못하는 현대인들, 이제는 디지털도 다이어트를 해보는 건 어떨까요?**

**쉽지 않은 도전을 코스믹 디톡스가 도와줄게요.**





## 2. 기술 스택

<br>

| 범위 | 기술 이름 |
| --- | --- |
| Architecture | Clean Architecture |
| Design Pattern | MVVM , Repository |
| DI | Hilt |
| Async Task | Coroutine , Flow |
| Firebase | Authentication, Firestore, Functions, Storage |
| Local DB | SharedPreferences |
| Component |	Service , Broadcast Receiver | 
| UI | Jetpack Navigation, XML |
| Image | Glide |
| 외부 API |  |

</br>





## 3. 주요 기능 
![image](https://github.com/user-attachments/assets/3b3ccd67-29ff-40e4-a867-061dac4ac63a)
<details>
  <summary>
   더보기  </summary> 
  회원가입 및 로그인을 할 수 있습니다.
카카오 로그인 구글 로그인 X 로그인 총 3개의 소셜 로그인을 지원합니다.
</details>




![image](https://github.com/user-attachments/assets/52b57024-6eda-4f5d-83a2-5da4d3589196)
<details>
  <summary>
    더보기   </summary> 
홈 화면에서는 나의 디지털 디톡스 이력을 한 눈에 조회할 수 있습니다.
총 누적 시간과 누적 조회수에 따라 커지는 행성을 통해 직관적으로 알 수 있으며 오늘 누적 시간 또한 별도로 표시하고 있습니다.
</details>




![image](https://github.com/user-attachments/assets/9d95b5f7-8ad6-483b-bf57-a71b4cf57722)
<details>
  <summary>
    더보기   </summary> 
  레이스 화면에서는 모든 유저의 누적시간을 1위부터 100위까지 조회할 수 있습니다.
  내 순위는 하단에 고정되어 있습니다
</details>
 

![image](https://github.com/user-attachments/assets/bd175b89-b85f-4001-8aec-4ee5dc83ba02)
<details>
  <summary>
    더보기  </summary> 
  내정보 화면에서는 앱 이용기간과 총 누적 시간, 트로피의 유무를 볼 수 있으며, 나의 전체 앱 사용량을 조회할 수 있습니다.

 또한 디지털 디톡스 중에 사용할 **`허용 앱`** 을 설정할 수 있습니다. **`허용 앱`** 을 설정해야 디톡스 중에서 다른 앱을 이용할 수 있으며, 기본 고정 시간은 30분입니다. **`앱 사용시간 제한`** 기능을 통해 **`허용 앱`** 들의 사용 시간을 커스텀 할 수 있습니다.
</details>


![image](https://github.com/user-attachments/assets/34aa20ef-7eb0-4592-96e5-e4a79cf5bb32)

![image](https://github.com/user-attachments/assets/1229d9a3-3c9d-49b7-9313-a2f2dae1ea54)
<details>
  <summary>
    더보기   </summary> 
   타이머를 시작하면 디지털 디톡스를 시작하는 것과 같고 측정된 시간은 총 누적 시간과 오늘 하루 디톡스 시간에 쌓이게 됩니다.
   타이머가 돌아가는 동안엔 뒤로가기나 홈버튼, 메뉴 버튼 등은 동작하지 않으며 화면을 벗어나려고 하면 OverlayView 가 나오며 동작을 제한합니다.
   하지만 앱 사용 중에 다른 앱을 사용하고 싶으면 잠깐 쉬기 기능을 통해 설정해두었던 허용 앱 에 접근하여 제한 시간 만큼만 쉴 수 있습니다. 하지만 이미 제한 시간을 모두 소모한 경우엔 더 이상 해당 앱을 사용할 수 없습니다.

</details>

## 4. 기술적 의사결정

<details>
  <summary>
    ❓ 허용 앱 리스트의 경우 왜 로컬 DB가 아닌 FireStore를 사용했나요?  </summary> 
  
1. 다중 기기 사용 가능성
→ 디톡스 앱 사용자 중에는 공부나 일에 집중하기 위해 사용하는 사람들이 많습니다. 특히 학생이나 공시생의 경우, 스마트폰과 함께 태블릿으로 인터넷 강의를 듣는 경우가 많습니다.
→ 여러 기기에서 앱을 사용할 수 있게 하려면 동일한 계정으로 로그인했을 때, 기기마다 동일한 허용 앱 리스트가 있어야 합니다. 이를 위해 로컬 DB 대신 Firestore를 사용하여 서버에서 데이터를 관리하는 방식을 선택했습니다.

2. 시간 초기화 기능
→ 자정 마다 허용 앱 사용 시간을 초기화하려면 각 앱에 지정된 제한 시간을 서버가 알고 있어야 하기 때문에, Firestore에서 데이터를 관리하는 것이 더 적합하다고 판단했습니다.

3. 앱 삭제 시 데이터 초기화
→ 사용자가 앱을 삭제하고 다시 설치하면 로컬 DB의 데이터는 삭제되지만, Firestore를 사용하면 기존 허용 앱 리스트가 서버에 저장되어 유지됩니다. 이를 통해 사용자는 앱을 다시 설치해도 동일한 허용 앱 리스트를 복원할 수 있어, 데이터를 안전하게 관리할 수 있습니다.</details>


<details>
  <summary>
    ❓ 왜 Hilt를 사용했나요?  </summary> 
  
1. Context의 효율적인 관리
→ Hilt는 Context를 효율적으로 관리할 수 있습니다. 안드로이드의 PackageManager나 UsageStatsManager와 같은 시스템 서비스들은 Context를 필요로 하는데, Hilt를 사용해 Application의 Context를 전역적으로 주입함으로써 필요한 객체들을 간편하게 사용할 수 있습니다. 이를 통해 Presentation 레이어로부터 Context를 직접적으로 전달받지 않아도 됩니다.

2. 외부 서비스(Firebase 등)의 전역 관리
→ Firebase와 같은 외부 서비스들도 Hilt를 통해 주입되어 여러 컴포넌트에서 동일한 인스턴스를 활용할 수 있습니다. Firestore, Firebase Auth, Firebase Functions 등은 전역적으로 한 번만 생성하고, 앱 전체에서 사용할 수 있도록 효율적으로 관리할 수 있어 성능 최적화와 코드를 간결하게 했습니다.

3. 코드 가독성 및 유지보수성
→ Hilt를 사용해 @Provides, @Binds 어노테이션을 통해 필요한 객체들을 명시적으로 주입함으로써, Repository나 ViewModel과 같은 컴포넌트에 직접 의존성을 관리할 필요 없이 Hilt가 자동으로 관리하게 합니다. </details>



<details>
  <summary>
    ❓ 왜 LiveData가 아닌 Flow를 사용했나요?  </summary> 
  
1. Firestore DB 구조 설계
→ 유저 정보는 users 컬렉션 내에 uid로 정의된 문서에 저장하고, 허용 앱 리스트는 리스트 타입 필드 대신 apps라는 서브 컬렉션으로 설계했습니다. 화면마다 필요한 데이터가 다르기 때문입니다.
→ 마이페이지에서는 유저의 전체 정보가 필요하므로, 유저 문서에서 한 번에 모든 정보를 불러와야 합니다.
→ 타이머 화면에서는 허용된 앱 리스트만 필요하기 때문에, 유저 문서의 모든 필드를 불러오는 것보다 apps 서브 컬렉션만 읽어오는 것이 더 낫다고 판단했습니다. 이를 통해 데이터 호출 시 필요한 리소스를 줄일 수 있고, 유지보수 측면에서도 허용 앱 리스트의 변경이 더 쉽습니다.

2. 데이터 스트림의 용이성
→ 유저 정보는 users 컬렉션의 문서에서 불러오고, 허용 앱 리스트는 apps 서브 컬렉션에서 불러오는 구조인데, Flow는 데이터 스트림을 통해 여러 소스로부터 데이터를 병합하고 관리할 수 있습니다.
→ 네트워크에서 데이터를 가져올 때, Flow의 콜드 스트림 특성 덕분에 데이터를 필요할 때만 가져올 수 있어 리소스를 효율적으로 관리할 수 있습니다. 예를 들어, Flow API의 zip 과 같은 오퍼레이터를 사용해 유저 정보와 허용 앱 리스트를 동시에 가져와 병합 처리할 수 있습니다.</details>



<details>
  <summary>
 ❓ 자정이 되거나 핸드폰의 전원이 꺼질 경우 타이머를 어떻게 처리했나요?  </summary> 
  
> **1. AlarmManager와 Broadcast Receiver 사용**
> 
> - 해설 코드
>     
>     자정이 되거나 기기의 전원이 꺼졌다가 다시 켜질 경우, 타이머를 자동으로 초기화하거나 재설정하기 위해 **AlarmManager**와 **BroadcastReceiver**를 사용했습니다. 
>     
>     ### **1. 자정에 타이머를 초기화하는 방법**
>     
>     자정이 되면 타이머가 자동으로 초기화되도록 설정하기 위해 **AlarmManager**를 이용하여 자정에 호출되는 알람을 설정했습니다. 자정이 되면 **MidnightResetReceiver**가 호출되고, 해당 리시버는 타이머를 초기화하도록 설계했습니다.
>     
>     **MidnightResetReceiver 동작 방식**
>     
>     - 자정이 되면 타이머 초기화를 위한 서비스 (`TimerService`)를 호출하고, 타이머를 초기화합니다.
>     - 또한 다음 자정을 위해 다시 알람을 설정합니다.
>     
>     ```kotlin
>     class MidnightResetReceiver : BroadcastReceiver() {
>         override fun onReceive(context: Context?, intent: Intent?) {
>             val resetIntent = Intent(context, TimerService::class.java).apply {
>                 action = TimerService.ACTION_RESET_TIMER
>             }
>             context?.startService(resetIntent)  // TimerService에 초기화 요청
>     
>             // 다음 자정을 위한 알람 재설정
>             val app = context.applicationContext as CosmicDetoxApplication
>             app.scheduleExactAlarm(context)  // 다음 자정을 위해 다시 알람 설정
>         }
>     }
>     ```
>     
>     ### 2. **핸드폰이 꺼졌다가 켜졌을 때의 처리**
>     
>     기기가 재부팅되거나 전원이 꺼졌다가 다시 켜지면, **AlarmManager**에 설정된 알람들이 모두 초기화되므로, 이때 자정을 위한 알람을 다시 설정해야 합니다. 이를 처리하기 위해 **BootCompletedReceiver**를 사용했습니다.
>     
>     - **BootCompletedReceiver 동작 방식**:
>         - 기기가 재부팅되면 `ACTION_BOOT_COMPLETED` 브로드캐스트를 수신하여, 다시 자정을 위한 알람을 재설정합니다.
>     
>     ```kotlin
>     class BootCompletedReceiver : BroadcastReceiver() {
>         override fun onReceive(context: Context, intent: Intent) {
>             if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
>                 val app = context.applicationContext as CosmicDetoxApplication
>                 app.scheduleExactAlarm(context)  // 재부팅 후 알람 재설정
>             }
>         }
>     }
>     
>     ```
>     
>     ### 3. **타이머 초기화 처리**
>     
>     타이머 초기화는 **TimerService** 내에서 수행되며, 자정에 도달했을 때 또는 기기가 다시 부팅되었을 때 타이머가 정상적으로 초기화됩니다.
>     
>     - **resetTimer()** 메소드를 사용하여 현재 타이머 상태를 저장하고, `dailyTime`을 초기화한 후 타이머를 다시 시작합니다.

</details>

<details>
  <summary>
❓ 허용 앱 사용 시간을 리마인드하기 위해 알람을 어떻게 보냈나요?  </summary> 

1. Foreground Service 사용
→ 허용 앱을 사용 중이므로 현재 우리 앱은 활성화 되어있지 않고, 허용 앱 사용시간이 5분 내로 남았을 경우 알람을 보낼 수 있어야 했습니다. 우리 앱은 백그라운드 상태에서 꾸준히 실행되고 있는 상태이기 때문에 Foreground Service를 사용하여 알람을 전송할 수 있도록 하였습니다.</details>




<details>
  <summary>
❓ 왜 Firebase Functions를 사용했나요?  </summary> 

1. Custom Token 사용을 위해서
→ kakao sign in api를 이용하고 나면 전용 token이 발급되는데, 이 token을 firebase authentication에 바로 사용할 수 없고 custom token을 만들어야 합니다.  Custom Token을 만드는 데에 서버가 필요하여 Firebase Functions을 사용해서  kakao sign in api와 custom token 제작을 동시에 진행하고, android에서 custom token을 받아올 수 있도록 해주었습니다.</details>



## 5. 트러블 슈팅

<details>
  <summary>
    추가하지 않은 광고 ID 권한이 포함되어있는 경우  </summary> 
  
  ### ⛔️문제사항
  ![image](https://github.com/user-attachments/assets/0a7935b4-85e6-45da-a078-8f5ca926d750)



구글 배포를 하려고 aab 파일을 등록해놓고 보니 광고를 사용하지 않음에도 이러한 에러가 나왔다. 분명 우리 Manifest 파일에는 광고 권한을 기재하지 않았는데도 '예'로 응답하라고 한다!

### ✅ 해결방안

**Firebase crashlytics**를 앱에 연동했거나, **analytics**를 연동했다면 광고 권한이 자동으로 들어간다고 한다. 우리는 Firebase를 적극적으로 활용한 경우이기 때문에 아무래도 광고 권한이 들어간 모양이다.

'예'로 응답하고 아래 체크박스에서 애널리틱스로 체크해준 뒤 심사에 맡기면 된다.
</details>


<details>
  <summary>
    ❓   </summary> 
  
</details>

<details>
  <summary>
    ❓   </summary> 
  
</details>






































## 6. 구성원




