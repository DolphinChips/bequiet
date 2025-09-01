package bequiet.http.app.templates

import scalatags.Text.all.{title => _, *}
import scalatags.Text.tags2.title
import bequiet.domain.chart.Chart
import bequiet.domain.song.Song

def charts(c: List[(Chart, Option[Float])], song: Song) = doctype("html")(
  html(lang := "en")(
    head()(
      title(s"Charts for ${song.title}")
    ),
    body(
      for (chart, rating) <- c
      yield div(
        chart.difficulty.toString,
        rating.map(_.toString).getOrElse("-"),
        br()
      )
    )
  )
)
