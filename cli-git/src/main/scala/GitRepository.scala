/*
 * Original implementation (C) 2010-2015 Nathan Hamblen and contributors
 * Adapted and extended in 2016 by foundweekends project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package giter8

import scala.util.matching.Regex
import java.io.File
import java.net.URI

sealed trait GitRepository

object GitRepository {
  case class Local(path: File) extends GitRepository

  case class Remote(url: String) extends GitRepository

  case class GitHub(user: String, repo: String) extends GitRepository {
    def publicUrl: String  = s"https://github.com/$user/$repo.g8.git"
    def privateUrl: String = s"git@github.com:$user/$repo.g8.git"
  }

  def fromString(string: String): Either[String, GitRepository] = string match {
    case Matches.Local(_)           => Right(Local(toFile(new URI(string))))
    case Matches.NativeUrl(url)     => Right(Remote(url))
    case Matches.HttpsUrl(url)      => Right(Remote(url))
    case Matches.HttpUrl(url)       => Right(Remote(url))
    case Matches.SshUrl(url)        => Right(Remote(url))
    case Matches.GitHub(user, repo) => Right(GitHub(user, repo))
    case _                          => Left(s"unknown repository type: $string")
  }

  object Matches {
    val GitHub: Regex    = """^([^\s/]+)/([^\s/]+?)(?:\.g8)?$""".r
    val Local: Regex     = """^file://(\S+)$""".r
    val NativeUrl: Regex = "^(git[@|://].*)$".r
    val HttpsUrl: Regex  = "^(https://.*)$".r
    val HttpUrl: Regex   = "^(http://.*)$".r
    val SshUrl: Regex    = "^(ssh://.*)$".r
  }

  def toFile(uri: URI): File = {
    assert(
      Option(uri.getScheme) match {
        case None | Some("file") => true
        case _ => false
      },
      s"Expected protocol to be 'file' or empty in URI $uri"
    )
    val part = uri.getSchemeSpecificPart
    if (!(part startsWith "/") && (part contains ":")) new File("///" + part)
    else {
      if (part.startsWith("//")) new File(part.drop(2))
      else new File(part)
    }
  }
}
