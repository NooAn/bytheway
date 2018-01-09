package ru.a1024bits.bytheway.model

/**
 * Created by Andrei_Gusenkov on 12/19/2017.
 * Response holder provided to the UI
 *
 * @param <T>
 */
enum class Status {
    SUCCESS,
    ERROR
}

class Response<T> private constructor(val status: Status, val data: T?, val error: Throwable?) {
    companion object {

        fun <T> success(data: T): Response<T> {
            return Response(Status.SUCCESS, data, null)
        }

        fun <T> error(error: Throwable): Response<T> {
            return Response<T>(Status.ERROR, null, error)
        }
    }
}