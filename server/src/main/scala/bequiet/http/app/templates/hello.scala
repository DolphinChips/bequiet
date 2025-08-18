package bequiet.http.app.templates

import scalatags.Text.all.*

def hello(who: String) =
  doctype("html")(
    html(lang := "en")(
      body(
        h1("hello"),
        span(who)
      )
    )
  )
