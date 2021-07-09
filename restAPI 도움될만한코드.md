### 0) API 양식

#####  **API 응답 포맷**

정상처리 및 오류처리에 대한 API 서버 공통 응답 포맷을 아래와 같이 정의 합니다.

\- 정상처리 및 오류처리 모두 success 필드를 포함합니다.

 \* 정상처리라면 true, 오류처리라면 false 값을 출력합니다.

\- 정상처리는 response 필드를 포함하고 error 필드는 null 입니다.

 \* 응답 데이터가 `단일 객체`라면, response 필드는 `JSON Object`로 표현됩니다.

 \* 응답 데이터가 `스칼라 타입(string, int, boolean)`이라면, response 필드는 `string, int, boolean로 표현`됩니다.

 \* 응답 데이터가 `Collection`이라면, response 필드는 `JSON Array`로 표협됩니다.

\- 오류처리는 error 필드를 포함하고 response 필드는 null 입니다. error 필드는 status, message 필드를 포함합니다.

 \* status : HTTP Response status code 값과 동일한 값을 출력해야 합니다.

 \* message : 오류 메시지가 출력 됩니다.

<br>

### 1) ApiUtils 클래스

API 양식에 맞춰서 응답 포맷을 반환해주는 클래스.

```java
package com.github.prgrms.utils;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.http.HttpStatus;

public class ApiUtils {

  public static <T> ApiResult<T> success(T response) {
    return new ApiResult<>(true, response, null);
  }

  public static ApiResult<?> error(Throwable throwable, HttpStatus status) {
    return new ApiResult<>(false, null, new ApiError(throwable, status));
  }

  public static ApiResult<?> error(String message, HttpStatus status) {
    return new ApiResult<>(false, null, new ApiError(message, status));
  }

  public static class ApiError {
    private final String message;
    private final int status;

    ApiError(Throwable throwable, HttpStatus status) {
      this(throwable.getMessage(), status);
    }

    ApiError(String message, HttpStatus status) {
      this.message = message;
      this.status = status.value();
    }

    public String getMessage() {
      return message;
    }

    public int getStatus() {
      return status;
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .append("message", message)
        .append("status", status)
        .toString();
    }
  }

  public static class ApiResult<T> {
    private final boolean success;
    private final T response;
    private final ApiError error;

    private ApiResult(boolean success, T response, ApiError error) {
      this.success = success;
      this.response = response;
      this.error = error;
    }

    public boolean isSuccess() {
      return success;
    }

    public ApiError getError() {
      return error;
    }

    public T getResponse() {
      return response;
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .append("success", success)
        .append("response", response)
        .append("error", error)
        .toString();
    }
  }

}
```



```java
package com.github.prgrms.errors;

import com.github.prgrms.utils.ApiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.validation.ConstraintViolationException;

import static com.github.prgrms.utils.ApiUtils.error;

@ControllerAdvice
public class GeneralExceptionHandler {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private ResponseEntity<ApiUtils.ApiResult<?>> newResponse(Throwable throwable, HttpStatus status) {
    return newResponse(throwable.getMessage(), status);
  }

  private ResponseEntity<ApiUtils.ApiResult<?>> newResponse(String message, HttpStatus status) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "application/json");
    return new ResponseEntity<>(error(message, status), headers, status);
  }

  // 필요한 경우 적절한 예외타입을 선언하고 newResponse 메소드를 통해 응답을 생성하도록 합니다.
  @ExceptionHandler({
    NoHandlerFoundException.class,
    NotFoundException.class
  })
  public ResponseEntity<?> handleNotFoundException(Exception e) {
    return newResponse(e, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<?> handleUnauthorizedException(Exception e) {
    return newResponse(e, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler({
    IllegalArgumentException.class,
    IllegalStateException.class,
    ConstraintViolationException.class,
    MethodArgumentNotValidException.class
  })
  public ResponseEntity<?> handleBadRequestException(Exception e) {
    log.debug("Bad request exception occurred: {}", e.getMessage(), e);
    if (e instanceof MethodArgumentNotValidException) {
      return newResponse(
        ((MethodArgumentNotValidException) e).getBindingResult().getAllErrors().get(0).getDefaultMessage(),
        HttpStatus.BAD_REQUEST
      );
    }
    return newResponse(e, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(HttpMediaTypeException.class)
  public ResponseEntity<?> handleHttpMediaTypeException(Exception e) {
    return newResponse(e, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<?> handleMethodNotAllowedException(Exception e) {
    return newResponse(e, HttpStatus.METHOD_NOT_ALLOWED);
  }

  @ExceptionHandler({Exception.class, RuntimeException.class})
  public ResponseEntity<?> handleException(Exception e) {
    log.error("Unexpected exception occurred: {}", e.getMessage(), e);
    return newResponse(e, HttpStatus.INTERNAL_SERVER_ERROR);
  }

}
```

