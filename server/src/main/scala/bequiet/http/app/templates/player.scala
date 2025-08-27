package bequiet.http.app.templates

import scalatags.Text.all.{title => _, *}
import scalatags.Text.tags2.title
import bequiet.domain.player.Player

def player(p: Player) = doctype("html")(
  html(lang := "en")(
    head()(title(p.djName)),
    body(
      h1(p.djName),
      span(s"UUID: ${p.id} "),
      a(href := s"/player/${p.id}/scores")("Scores")
    )
  )
)
