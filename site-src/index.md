---
name: boot-server
description: A fork of the (unmaintained) Boot `server` task with community PRs integrated and support for Immutant added. Originally from pandeiro/boot-http.
---
## Latest version

* [The Code](https://github.com/coconutpalm/boot-server)
* [Codox](codox/index.html) generated from the code
* Latest binary release: [![Clojars Project](https://img.shields.io/clojars/v/coconutpalm/boot-server.svg)](https://clojars.org/coconutpalm/boot-server)

## Community support

This project exists because the original maintainer stopped maintaining the
code and/or responding to PR requests.  I don't want that to happen here, so:

* I'm establishing some parameters for PRs to be accepted.
* If I cannot respond in a resonable time (given this is a solo effort and I 
  have a day job not involving Clojure/script), I invite anyone interested in
  continuing this effort to file a ticket against the project along with a PR
  showing you can be trusted to further this effort in a responsible manner.
  If I can't respond to that ticket in a timely manner and there aren't extenuating
  circumstances (e.g.: a death in the family), I'll transfer project ownership to you.

### PR checklist

There are two kinds of PRs:

* Work-in-progress (WIP) PRs where a developer wants committer feedback before
  investing time to create a full PR.
* A full PR that is ready to review and merge.

With that in mind, I would greatly appreciate if PRs follow this checklist:

* WIP PRs include (WIP) at the beginning fo the title.  Otherwise:
* Include tests and/or justification(s) for no tests.
* Update CHANGELOG in the form already in the file.
* Update the project version number in:
  * build.boot
  * The project documentation: README.md and/or the web site markdown.
* If a change is a breaking change, update the minor version number; else update the patchlevel version number.
* Update project documentation with any new instructions required to use your new feature.

### Desired PRs

* Anything improving test coverage / testability.

* Previously the community had written Aleph support in several earlier PRS but none of
those efforts merge cleanly anymore.  If interested parties fix that I'd be happy
to consider PRs re-adding this functionality.

* Previous community efforts ditched the individual options for selecting a server other
than Jetty, instead consolidating these into a single flag --server="http-kit", etc.
I would welcome efforts to re-add this feature also.

## Usage

(The following examples assume you have [boot installed][installboot] and updated.)

### Command line, no setup

This serves the **current directory** at port **3000**:

```bash
boot -d coconutpalm/boot-server serve -d . wait
```

To inspect the meanings of the flags and other tasks, use boot's built-in
documentation mechanism:

```bash
boot -d coconutpalm/boot-server -h       # show all tasks on the classpath
boot -d coconutpalm/boot-server serve -h # show serve's usage
```

### Within a project

If you already have a `build.boot`, add
`coconutpalm/boot-server` to `:dependencies`
and `(require '[coconutpalm.boot-http :refer :all])`.

You can use boot-http for three different use cases:

#### 1. Serve classpath resources

```bash
boot serve wait   # or from REPL: (boot (serve) (wait))
```

#### 2. Serve files on chosen directory

```bash
boot serve -d target wait   # or at the REPL: (boot (serve :dir "target") (wait))
```

That would serve the `target` directory if it exists.

Instead of specifying a directory, you can also specify a ring handler:

#### 3. Start server with given Ring handler

```bash
boot serve -H myapp.server/app -R wait   # (boot (serve :handler 'myapp.server/app :reload true) (wait))
```

Note: boot-http will automatically wrap responses in middlewares to
add content type and charset (see `--charset` option) to response
headers.

Note: The handler symbol must include a fully qualified namespace as shown in
the example above (due to a limitation in boot). If the task is throwing
java.lang.NullPointerException's this is most likely the cause.

### Composability

You may have noticed the `wait` task being used after all the
command-line invocations so far. This is because by itself, the
`serve` task does not block and thus exits immediately.

What good is that? It means you can compose with other tasks.

In [boot-cljs-example][boot-cljs-example], for example, `serve` is
invoked like so:

```bash
boot serve watch speak reload cljs-repl cljs -usO none
```

which is, again, the same as:

```clojure
(comp (serve)
      (watch)
      (speak)
      (reload)
      (cljs-repl)
      (cljs :optimizations :none))
```

In that case, since `serve` is given a directory, it serves the directory and whatever
resources can be found on the classpath, and then gets out of the way.

### Other options

#### -p / --port

Use a specific port. A value of `0` will automatically bind to a free port.
The actual port number being used is available as `:http-port` on the fileset.

```bash
boot -d coconutpalm/boot-server serve -d . -p 8888 wait
```

Note: If this is specified, it overrides any :port inside the ssl-props parameter.

#### -k / --httpkit

Use the HTTP Kit webserver instead of Jetty.

```bash
boot -d coconutpalm/boot-server serve -d . -k wait  # uses httpkit
```

#### -I / --immutant

Use the Immutant webserver instead of Jetty.  Immutant supports both websockets and https.
See also: [Sente](https://github.com/ptaoussanis/sente)

```bash
boot -d coconutpalm/boot-server serve -d . -I wait  # uses immutant
```

#### -n / --nrepl

Start an nREPL server for access to the http server. Accepts
`:port` and `:bind` options for setting nREPL server IP
and port.

```bash
boot -d coconutpalm/boot-server serve -d . -n "{:port 3001}"
```

#### -i / --init and -c / --cleanup

Setup and teardown functions to run.

#### -s / --silent

Silences all output.

#### -R / --reload

Wrap provided ring handler with ring-reload.

#### -N / --not-found

Use the provided symbol's function to handle requests for results that
are not found.

```bash
boot serve -d target -N myapp.server/custom-not-found wait
```

Note: Just like the ring handler, the not-found handler also requires a symbol
with a fully qualified namespace, even if invoked from a build.boot file.

#### -S / --charset

Charset to use when serving resources or files. Defaults to UTF-8.

#### -t / --ssl

Serve over HTTPS (see `-T / --ssl-props` as well): Jetty/Immutant only.

## API and Roadmap

Right now that is about it. It basically blends the functionality of
`python3 -m http.server` and a subset of `lein ring server`.

Feel free to add issues or comment [here][boot-discourse] if
you have any ideas.


## Acknowledgements

The boot guys basically wrote all of this or walked me through any
parts I had to change. Thanks!


## License

Copyright © 2015 Murphy McMahon, 2018 David Orme

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

[boot]:              https://github.com/boot-clj/boot
[boot-cljs-example]: https://github.com/adzerk/boot-cljs-example
[installboot]:       https://github.com/boot-clj/boot#install
[boot-discourse]:    http://hoplon.discoursehosting.net/t/boot-http-0-4-0/361
[build]:             https://travis-ci.org/coconutpalm/boot-server
[badge]:             https://travis-ci.org/coconutpalm/boot-server.png?branch=master

