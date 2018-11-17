package io.finch.oauth2

import cats.effect.IO
import com.twitter.finagle.http.Status
import com.twitter.finagle.oauth2._
import com.twitter.util.Future
import io.finch._
import io.finch.catsEffect._
import org.mockito.Mockito._
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.prop.Checkers

class OAuth2Spec extends FlatSpec with Matchers with Checkers with MockitoSugar {

  behavior of "OAuth2"

  it should "authorize the requests" in {
    val at: AccessToken = AccessToken("foo", None, None, None, null)
    val dh: DataHandler[Int] = mock[DataHandler[Int]]
    val ai: AuthInfo[Int] = new AuthInfo(42, "foo", None, None)

    when(dh.findAccessToken("bar")).thenReturn(Future.value(Some(at)))
    when(dh.isAccessTokenExpired(at)).thenReturn(false)
    when(dh.findAuthInfoByAccessToken(at)).thenReturn(Future.value(Some(ai)))

    val authInfo: Endpoint[IO, AuthInfo[Int]] = authorize(dh)
    val e: Endpoint[IO, Int] = get("user" :: authInfo) { ai: AuthInfo[Int] =>
      Ok(ai.user)
    }

    val i1 = Input.get("/user", "access_token" -> "bar")
    val i2 = Input.get("/user")

    e(i1).awaitOutputUnsafe() shouldBe Some(Ok(42))
    val Some(error) = e(i2).awaitOutputUnsafe()
    error.status shouldBe Status.BadRequest
    error.headers should contain key "WWW-Authenticate"
  }

  it should "issue the access token" in {
    val dh: DataHandler[Int] = mock[DataHandler[Int]]
    val at: AccessToken = AccessToken("foobar", None, None, None, null)

    when(dh.validateClient("id", "", "password")).thenReturn(Future.value(true))
    when(dh.findUser("u", "p")).thenReturn(Future.value(Some(42)))
    when(dh.getStoredAccessToken(AuthInfo(42, "id", None, None))).thenReturn(Future.value(Some(at)))
    when(dh.isAccessTokenExpired(at)).thenReturn(false)

    val grandHandlerResult: Endpoint[IO, GrantResult] = issueAccessToken(dh)
    val e: Endpoint[IO, String] = get("token" :: grandHandlerResult) { ghr: GrantResult =>
      Ok(ghr.accessToken)
    }

    val i1 = Input.get("/token",
      "grant_type" -> "password", "username" -> "u", "password" -> "p", "client_id" -> "id"
    )

    val i2 = Input.get("/token")

    e(i1).awaitOutputUnsafe() shouldBe Some(Ok("foobar"))
    val Some(error) = e(i2).awaitOutputUnsafe()
    error.status shouldBe Status.BadRequest
    error.headers should contain key "WWW-Authenticate"
  }
}
