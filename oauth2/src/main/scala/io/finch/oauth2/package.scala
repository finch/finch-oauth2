package io.finch

import cats.effect.Effect
import cats.implicits._
import com.twitter.finagle.OAuth2
import com.twitter.finagle.http.Status
import com.twitter.finagle.oauth2.{AuthInfo, DataHandler, GrantResult, OAuthError}
import com.twitter.util.{Future => TwitterFuture, Return, Throw}

package object oauth2 {

  private def twitterFutureToEffect[F[_], A](f: => TwitterFuture[A])(implicit F: Effect[F]): F[A] =
    F.async { cb =>
      f.respond {
        case Return(r) => cb(Right(r))
        case Throw(t) => cb(Left(t))
      }
    }

  private val handleOAuthError: PartialFunction[Throwable, Output[Nothing]] = {
    case e: OAuthError =>
      val bearer = Seq("error=\"" + e.errorType + "\"") ++
        (if (!e.description.isEmpty) Seq("error_description=\"" + e.description + "\"") else Nil)

      Output.failure(e, Status(e.statusCode))
        .withHeader("WWW-Authenticate" -> s"Bearer ${bearer.mkString(", ")}")
  }

  /**
   * An [[Endpoint]] that takes a request (with access token) and authorizes it with respect to a
   * given `dataHandler`.
   */
  def authorize[F[_], U](dataHandler: DataHandler[U])(
    implicit F: Effect[F]
  ): Endpoint[F, AuthInfo[U]] =
    new Endpoint[F, AuthInfo[U]] {
      final def apply(input: Input): Endpoint.Result[F, AuthInfo[U]] = {
        val out = twitterFutureToEffect(OAuth2.authorize(input.request, dataHandler))
            .map(ai => Output.payload(ai))
            .recover(handleOAuthError)

        EndpointResult.Matched(input, Trace.empty, out)
      }
    }

  /**
   * An [[Endpoint]] that takes a request (with user credentials) and issues an access token for it
   * with respect to a given `dataHandler`.
   */
  def issueAccessToken[F[_], U](dataHandler: DataHandler[U])(
    implicit F: Effect[F]
  ): Endpoint[F, GrantResult] =
    new Endpoint[F, GrantResult] {
      final def apply(input: Input): Endpoint.Result[F, GrantResult] = {
        val out = twitterFutureToEffect(OAuth2.issueAccessToken(input.request, dataHandler))
          .map(ai => Output.payload(ai))
          .recover(handleOAuthError)

        EndpointResult.Matched(input, Trace.empty, out)
      }
    }
}
