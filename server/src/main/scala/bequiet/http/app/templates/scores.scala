package bequiet.http.app.templates

import scalatags.Text.all.{title => _, *}
import scalatags.Text.tags2.title
import bequiet.domain.score.Score

def scores(s: List[Score]) =
  val renderedScores =
    s.flatMap(score => List(span(s"id: ${score.id}, lamp: ${score.lamp}"), br))
  doctype("html")(
    html(lang := "en")(head()(title("Scores")), body(renderedScores))
  )
