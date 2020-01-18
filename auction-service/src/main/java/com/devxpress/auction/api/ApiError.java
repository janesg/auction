package com.devxpress.auction.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
@EqualsAndHashCode(exclude = {"isoDateTime"})
public final class ApiError {

    @ApiModelProperty(notes = "HTTP Status")
    private String status;
    @ApiModelProperty(notes = "HTTP Status Code", position = 1)
    private Integer statusCode;
    @ApiModelProperty(notes = "Error Code", position = 2)
    private Integer code;
    @ApiModelProperty(notes = "Error Message", position = 3)
    private String message;
    @ApiModelProperty(notes = "UTC Timestamp (ISO 8601 format)", position = 4)
    private String isoDateTime;
    @ApiModelProperty(notes = "Error Context Details", position = 5)
    private List<String> contextDetails;

    private ApiError(HttpStatus status) {
        this.status = status.name();
        this.statusCode = status.value();
    }

    public static final class ApiErrorBuilder {

        private HttpStatus status;
        private Integer code;
        private String message;
        private List<String> contextDetails = new ArrayList<>();

        private ApiErrorBuilder() {}

        private ApiErrorBuilder withStatus(HttpStatus status) {
            this.status = status;
            return this;
        }

        public static ApiErrorBuilder createInstance(HttpStatus status) {
            ApiErrorBuilder builder = new ApiErrorBuilder();
            return builder.withStatus(status);
        }

        public ApiErrorBuilder withCode(Integer code) {
            this.code = code;
            return this;
        }

        public ApiErrorBuilder withMessage(String message) {
            this.message = message;
            return this;
        }

        public ApiErrorBuilder withContextDetail(String contextDetail) {
            this.contextDetails.add(contextDetail);
            return this;
        }

        public ApiError build() {
            ApiError error = new ApiError(this.status);
            error.code = this.code;
            error.message = this.message;
            // Following has same effect as : ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
            error.isoDateTime = Instant.now().toString();
            error.contextDetails = new ArrayList<>(this.contextDetails);
            return error;
        }
    }
}
