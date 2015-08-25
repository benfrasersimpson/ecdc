package ecdc.aws

import com.amazonaws.AmazonWebServiceRequest
import com.amazonaws.handlers.AsyncHandler
import java.util.concurrent.{ Future ⇒ JFuture }
import scala.concurrent.{ Future, Promise }

package object ecs {
  private def promiseToAsyncHandler[Request <: AmazonWebServiceRequest, Result](p: Promise[Result]) =
    new AsyncHandler[Request, Result] {
      override def onError(exception: Exception): Unit = {
        p.failure(exception)
        ()
      }
      override def onSuccess(request: Request, result: Result): Unit = {
        p.success(result)
        ()
      }
    }

  private def promiseToVoidAsyncHandler[Request <: AmazonWebServiceRequest](p: Promise[Unit]) =
    new AsyncHandler[Request, Void] {
      override def onError(exception: Exception): Unit = {
        p.failure(exception)
        ()
      }
      override def onSuccess(request: Request, result: Void): Unit = {
        p.success(())
        ()
      }
    }

  @inline
  private[ecs] def wrapAsyncMethod[Request <: AmazonWebServiceRequest, Result](
    f: (Request, AsyncHandler[Request, Result]) ⇒ JFuture[Result],
    request: Request): Future[Result] = {
    val p = Promise[Result]()
    f(request, promiseToAsyncHandler(p))
    p.future
  }

  @inline
  private[ecs] def wrapVoidAsyncMethod[Request <: AmazonWebServiceRequest](
    f: (Request, AsyncHandler[Request, Void]) ⇒ JFuture[Void],
    request: Request): Future[Unit] = {
    val p = Promise[Unit]()
    f(request, promiseToVoidAsyncHandler(p))
    p.future
  }
}

