# Retrica-Color-Matrix

## 1. 프로젝트 소개
- **주제:** Android 앱 내 이미지 필터 기능 구현
- **목적:** 사용자에게 간단한 **필터 효과** 제공 (밝기 조절, 흑백 처리, 초기화)
- **기술 스택:**
    - **Android (Kotlin)**
    - **상태 관리**: Kotlin Flow & StateFlow
    - **아키텍처**: MVVM 패턴
    - **최소 SDK**: 21 (Android 5.0 Lollipop)
    - **타겟 SDK**: 35 (Android 15)
    - **UI 프레임워크**: View System (XML)

### **📌** 주요 기능 및 완성 화면
| 흑백 필터 | 밝기 증감 | 원본 되돌리기 |
|--------|--------|--------|
| <img src="https://github.com/user-attachments/assets/dc03000e-3cc0-4e00-a632-62f0a4a1a4e0" width="200"/> | <img src="https://github.com/user-attachments/assets/3429c573-8efc-46d6-a696-4f1cd81e5b53" width="200"/> | <img src="https://github.com/user-attachments/assets/cfc2c969-1b27-41ee-a549-c078904dfb5f" width="200"/> |



## 2. 필터 기능 구현 설명


**Color Matrix 활용으로 인한 RGB 수정.** 

Color Matrix vs Bitmap 직접 조작

- Q: ColorMatrix 방식의 성능이 좋은 이유는?
→ GPU 최적화, draw-time에 적용됨
- Q: Bitmap 직접 조작이 느린 이유는?
→ 각 픽셀마다 연산 + 메모리 복사, GC, UI thread 블로킹

### 🎨 ColorMatrix란?

**색상(RGB)과 알파(A)** 값을 변환하기 위한 **4x5 행렬**입니다. 이 행렬은 1차원 배열로 표현되며, 다음과 같은 구조를 가집니다:

```kotlin
[ a, b, c, d, e, -> Red
  f, g, h, i, j, -> Green
  k, l, m, n, o, -> Blue
  p, q, r, s, t ] -> Alpha
```

### 🎯 어떻게 적용되나요?

원본 색상 `[R, G, B, A]`에 대해 아래와 같이 변환된 색상이 계산됩니다:

```kotlin
R' = a*R + b*G + c*B + d*A + e
G' = f*R + g*G + h*B + i*A + j
B' = k*R + l*G + m*B + n*A + o
A' = p*R + q*G + r*B + s*A + t
```

이후 계산된 `[R', G', B', A']` 값은 각각 **0~255 사이로 Clamp(잘림)** 됩니다.


### 🧪 예시: 색상 반전

다음은 색상을 반전시키는 ColorMatrix입니다:

```kotlin
[ -1, 0, 0, 0, 255,
   0, -1, 0, 0, 255,
   0,  0, -1, 0, 255,
   0,  0,  0, 1,   0 ]
```

- RGB 값을 -1배 해서 색상을 반전시키고,
- 255를 더해 다시 0~255 색상 공간 안에 맞춥니다.
- 알파값(A)은 그대로 유지됩니다.


### 📌 사용처

`ColorFilter.colorMatrix`의 파라미터 사용되며, `Paint.colorFilter` (혹은 이미지뷰) 를 통해 **그리기 시점(draw time)** 에 적용됩니다.

왜 행렬곱인가?

→ 매번 필터에 따른 색상을 직접 지정하게 되면 연산이 복잡하기에 각 rgb값에 맞는 행렬 곱으로 하나의 행렬만 color filter에 할당하는 것이 더 효율적이기 때문.

## 필터 적용 방법

### ColorMatrix를 활용한 필터 구현

- Android의 `ColorMatrix` 클래스를 사용하여 이미지 필터링
- `ColorMatrixColorFilter`를 ImageView에 적용하여 실시간 필터 처리
- 원본 이미지의 픽셀 데이터를 직접 수정하지 않고 View 레벨에서 필터 적용 (메모리 효율성)

### 구현한 필터 종류

1. **흑백 필터**: `setSaturation(0f)`를 사용하여 구현
    
    ```kotlin
    
    fun toGrayScaleFilter(): ColorMatrix {
        sharedMatrix.reset()
        sharedMatrix.setSaturation(0f)
        return sharedMatrix
    }
    ```
    
2. **밝기 조절 필터**: 색상 행렬을 직접 설정하여 구현
    
    ```kotlin
    
    fun toBrightnessFilter(value: Float): ColorMatrix {
        sharedMatrix.reset()
        sharedMatrix.set(
            floatArrayOf(
                1f, 0f, 0f, 0f, value,
                0f, 1f, 0f, 0f, value,
                0f, 0f, 1f, 0f, value,
                0f, 0f, 0f, 1f, 0f,
            ),
        )
        return sharedMatrix
    }
    ```
    
3. **기본 필터(필터 제거)**: 행렬 초기화를 통한 구현
    
    ```kotlin
    
    fun toDefaultFilter(): ColorMatrix {
        sharedMatrix.reset()
        return sharedMatrix
    }
    ```


## 4. 고민한 부분: ColorMatrix 사용 시 싱글톤 인스턴스 활용

### 문제 상황

- 필터 적용 시 매번 새로운 ColorMatrix 객체를 생성하면 메모리 낭비 발생
- 빈번한 객체 생성은 GC 부담 증가로 이어질 수 있음

### 해결 방안

- ColorMatrixFactory를 싱글톤으로 구현하고 내부에 `sharedMatrix` 하나만 재사용
    
    ```kotlin
    object ColorMatrixFactory {
        private val sharedMatrix = ColorMatrix()
    
    // 필터 생성 메소드들은 모두 sharedMatrix를 재활용
    }
    ```
    

### 고려사항

- 싱글톤 패턴 사용 시 멀티스레드 환경에서 동시성 이슈 가능성
- 현재 구현에서는 UI 스레드에서만 접근하므로 문제 없음
- 향후 백그라운드 처리가 필요할 경우 동기화 메커니즘 추가 필요

## 질문사항


1. **싱글턴 인스턴스를 재사용 하는 경우에 있어 동시성 문제가 발생하는지 궁금합니다.**
    ![image](https://github.com/user-attachments/assets/7fa6ffed-c82a-4d3d-825b-e91ed0850cd6)
    

1. **필터를 먼저 선택한 후 카메라 촬영**
    
    레트리카의 경우 필터를 먼저 선택한 후 카메라를 촬영한 다는 점에서 차별점이 있는 것으로 알고 있습니다. 
    
    Q. 이 경우 뷰를 두개를 겹쳐서 미리보기 식으로 제공하는지?
    
    Q. 혹은 필터를 생성할때마다 비트맵도 같이 인스턴스화 해서 캐시에 미리 저장해 놓는지?
    
    어떠한 방식으로 필터를 미리 제공 하는지 궁금합니다!
    

**3.  postConcat을 사용한 필터 결합에 대한 의문**

- `ColorMatrix.postConcat()`을 사용하여 **흑백 필터와 밝기 조절을 동시에 적용**하고 싶었음
- 두 필터를 결합하는 방법과, 결합 순서에 따른 결과 차이가 아직 명확히 이해되지 않음
- → **학습 및 실험 필요**

Q. "밝기 증가 후 흑백 변환"과 "흑백 후 밝기 증가"의 결과는 어떻게 다를까?

## 5. 트러블슈팅: Channel 사용으로 인한 불필요한 팩토리 메소드 호출


### 문제 상황

- 초기 구현 시 Channel을 사용하여 필터 상태 변경 이벤트 전달
- 각 이벤트 수신(버튼 클릭)마다 매번 필터를 새로 생성하고 적용하는 과정에서 불필요한 연산 발생

### 해결 방안

- Channel 대신 StateFlow를 사용하여 상태 관리
- `flowWithLifecycle`을 통해 라이프사이클에 맞춰 효율적으로 상태 수집
    
    ```kotlin
    
    viewModel.filterState.flowWithLifecycle(lifecycle).onEach { event ->
        when (event) {
            is FilterState.Brightness -> applyBrightness(event.progress)
            FilterState.GrayScale -> applyGrayScale()
            FilterState.Default -> resetFilter()
        }
    }.launchIn(lifecycleScope)
    
    ```
    

### 개선 효과

- 불필요한 필터 생성 및 적용 감소
- UI 렌더링 성능 향상

## 6. 추후 확장성 및 필터 적용에 대한 생각


### 서버를 통해 수백 개의 필터를 지원하는 앱으로 확장 시 고려할 점

1. **필터 서버화**
    - JSON 등으로 ColorMatrix를 정의하고 서버에서 받아와 적용.
    
    ```kotlin
       fun toCustomFilter(matrix: FloatArray): ColorMatrix {
            sharedMatrix.reset()
            sharedMatrix.set(matrix)
            return sharedMatrix
        }
    ```
    
2. **캐싱 전략**
    - 자주 쓰는 필터는 로컬에 저장(하드코딩) 혹은 LRU캐시 기반 캐싱 전략 활용
3. **필터 결합**
    - 여러 필터를 조합해 새로운 필터 생성 가능한 구조
    - ColorMatrix의 postConcat() 활용하여 필터 효과 중첩

### 필터 적용에 대한 생각

> 사용자 가치
> 
> - 흑백 필터: 감성적인 분위기 연출, 디테일 강조
> - 밝기 조절: 어두운 사진을 밝게 보정

> 개발자로서의 가치
> 
> - 간단한 행렬 변환으로 다양한 필터 효과를 낼 수 있음
> - 하드코딩된 필터 외에도 동적/조합 필터의 가능성 큼(신규 기능 확장성)

## 소요 시간 및 학습


| 항목 | 내용 |
| --- | --- |
| 총 소요 시간 | 2일 |
| 주요 학습 | ColorMatrix 구조, 행렬 곱셈, 상태 관리 |
| 어려웠던 점 | postConcat 및 filter 합성 방식 |
