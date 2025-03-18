# Language Pack

1. [About](#about)
1. [Supported File Formats/Languages](#supported-fileformats)
1. [How to update grammars](#how-to-update)


## <a name="about"></a>About

Syntax highlighting and more for about 40 programming languages and file formats taken from [Visual Studio Code](https://github.com/microsoft/vscode/tree/main/extensions).

[TextMate grammars](https://en.wikipedia.org/wiki/TextMate#Language_Grammars) and
[language configurations](https://code.visualstudio.com/api/language-extensions/language-configuration-guide)
taken from [Visual Studio Code](https://github.com/microsoft/vscode/tree/main/extensions) and
provided as a plugin based on [Eclipse tm4e](https://github.com/eclipse-tm4e/tm4e)
for the [Eclipse IDE](https://eclipseide.org).


## <a name="supported-fileformats"></a>Supported File Formats/Languages

<!-- START-GENERATED -->

| Language/Format | File Associations | Source
|:--------------- |:----------------- |:------ |
| ASP.NET Razor | file-extensions="cshtml, razor" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/razor) [[upstream]](https://github.com/dotnet/razor/commit/9b1e979b6c3fe7cfbe30f595b9b0994d20bd482c)
| Batch File <img src="syntaxes/bat/bat.icon.png" width=16/> | file-extensions="bat, cmd" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/bat) [[upstream]](https://github.com/mmims/language-batchfile/commit/6154ae25a24e01ac9329e7bcf958e093cd8733a9)
| BibTeX | file-extensions="bib" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/latex) [[upstream]](https://github.com/jlelong/vscode-latex-basics/commit/c787db94a56bd93131ce0938046063320a02cc73)
| C | file-extensions="c, i" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/cpp) [[upstream]](https://github.com/jeff-hykin/better-c-syntax/commit/34712a6106a4ffb0a04d2fa836fd28ff6c5849a4)
| C# | file-extensions="cake, cs, csx" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/csharp) [[upstream]](https://github.com/dotnet/csharp-tmLanguage/commit/62026a70f9fcc42d9222eccfec34ed5ee0784f3d)
| C++ | file-extensions="c++, c++m, cc, ccm, cpp, cppm, cxx, cxxm, h, h++, h.in, hh, hpp, hpp.in, hxx, ii, inl, ino, ipp, ixx, tpp, txx" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/cpp) [[upstream]](https://github.com/jeff-hykin/better-cpp-syntax/commit/f1d127a8af2b184db570345f0bb179503c47fdf6)
| Clojure <img src="syntaxes/clojure/clojure.icon.png" width=16/> | file-extensions="clj, cljc, cljs, cljx, clojure, edn" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/clojure) [[upstream]](https://github.com/atom/language-clojure/commit/45bdb881501d0b8f8b707ca1d3fcc8b4b99fca03)
| Code Snippets | file-extensions="code-snippets"<br />file-patterns="\*\*/User/profiles/\*/snippets/\*.json, \*\*/User/snippets/\*.json" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/json) [[upstream]](https://github.com/jeff-hykin/better-snippet-syntax/commit/2b1bb124cb2b9c75c3c80eae1b8f3a043841d654)
| CoffeeScript <img src="syntaxes/coffeescript/coffeescript.icon.png" width=16/> | file-extensions="coffee, cson, iced" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/coffeescript) [[upstream]](https://github.com/atom/language-coffee-script/commit/0f6db9143663e18b1ad00667820f46747dba495e)
| CSS | file-extensions="css" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/css) [[upstream]](https://github.com/microsoft/vscode-css/commit/a927fe2f73927bf5c25d0b0c4dd0e63d69fd8887)
| CUDA C++ | file-extensions="cu, cuh" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/cpp) [[upstream]](https://github.com/NVIDIA/cuda-cpp-grammar/commit/81e88eaec5170aa8585736c63627c73e3589998c)
| Dart | file-extensions="dart" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/dart) [[upstream]](https://github.com/dart-lang/dart-syntax-highlight/commit/e1ac5c446c2531343393adbe8fff9d45d8a7c412)
| Diff | file-extensions="diff, patch, rej" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/diff) [[upstream]](https://github.com/textmate/diff.tmbundle/commit/0593bb775eab1824af97ef2172fd38822abd97d7)
| Docker | file-extensions="containerfile, dockerfile"<br />file-names="Containerfile, Dockerfile"<br />file-patterns="Containerfile.\*, Dockerfile.\*" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/docker) [[upstream]](https://github.com/moby/moby/commit/c2029cb2574647e4bc28ed58486b8e85883eedb9)
| F# | file-extensions="fs, fsi, fsscript, fsx" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/fsharp) [[upstream]](https://github.com/ionide/ionide-fsgrammar/commit/c62c78404d0b2c14816aae61ac0688663a5990a3)
| Git Commit Message <img src="syntaxes/git-base/icon.png" width=16/> | file-names="COMMIT_EDITMSG, MERGE_MSG" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/git-base) [[upstream]](https://github.com/walles/git-commit-message-plus/commit/35a079dea5a91b087021b40c01a6bb4eb0337a87)
| Git Ignore <img src="syntaxes/git-base/icon.png" width=16/> | file-extensions="git-blame-ignore-revs, gitignore, gitignore_global"<br />file-names=".gitignore" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/git-base)
| Git Rebase Message <img src="syntaxes/git-base/icon.png" width=16/> | file-names="git-rebase-todo"<br />file-patterns="\*\*/rebase-merge/done" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/git-base) [[upstream]](https://github.com/textmate/git.tmbundle/commit/5870cf3f8abad3a6637bdf69250b5d2ded427dc4)
| Go | file-extensions="go" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/go) [[upstream]](https://github.com/worlpaker/go-syntax/commit/fbdaec061157e98dda185c0ce771ce6a2c793045)
| Groovy | file-extensions="gradle, groovy, gvy, jenkinsfile, nf"<br />file-names="Jenkinsfile"<br />file-patterns="Jenkinsfile\*" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/groovy) [[upstream]](https://github.com/textmate/groovy.tmbundle/commit/85d8f7c97ae473ccb9473f6c8d27e4ec957f4be1)
| Handlebars | file-extensions="handlebars, hbs, hjs" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/handlebars) [[upstream]](https://github.com/daaain/Handlebars/commit/85a153a6f759df4e8da7533e1b3651f007867c51)
| HLSL (High-Level Shader Language) | file-extensions="cginc, compute, fx, fxh, hlsl, hlsli, psh, vsh" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/hlsl) [[upstream]](https://github.com/tgjones/shaders-tmLanguage/commit/87c0dca3a39170dbd7ee7e277db4f915fb2de14a)
| HTML | file-extensions="asp, aspx, ejs, htm, html, jshtm, jsp, mdoc, rhtml, shtml, volt, xht, xhtml" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/html) [[upstream]](https://github.com/textmate/html.tmbundle/commit/390c8870273a2ae80244dae6db6ba064a802f407)
| INI Config File | file-extensions="ini" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/ini) [[upstream]](https://github.com/textmate/ini.tmbundle/commit/2af0cbb0704940f967152616f2f1ff0aae6287a6)
| Java <img src="syntaxes/java/java.icon.png" width=16/> | file-extensions="jav, java" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/java) [[upstream]](https://github.com/redhat-developer/vscode-java/commit/f09b712f5d6d6339e765f58c8dfab3f78a378183)
| JavaScript | file-extensions="cjs, es6, js, mjs, pac"<br />file-names="jakefile" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/javascript) [[upstream]](https://github.com/microsoft/TypeScript-TmLanguage/commit/48f608692aa6d6ad7bd65b478187906c798234a8)
| JavaScript JSX | file-extensions="jsx" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/javascript) [[upstream]](https://github.com/microsoft/TypeScript-TmLanguage/commit/48f608692aa6d6ad7bd65b478187906c798234a8)
| JSON | file-extensions="bowerrc, css.map, geojson, har, ipynb, js.map, jscsrc, jslintrc, json, jsonld, ts.map, vuerc, webmanifest"<br />file-names=".watchmanconfig, composer.lock" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/json) [[upstream]](https://github.com/microsoft/vscode-JSON.tmLanguage/commit/9bd83f1c252b375e957203f21793316203f61f70)
| JSON Lines | file-extensions="jsonl, ndjson" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/json) [[upstream]](https://github.com/microsoft/vscode-JSON.tmLanguage/commit/9bd83f1c252b375e957203f21793316203f61f70)
| JSON with Comments | file-extensions="babelrc, eslintrc, eslintrc.json, hintrc, jsfmtrc, jshintrc, jsonc, swcrc"<br />file-names=".babelrc.json, .ember-cli, babel.config.json, bun.lock, typedoc.json" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/json) [[upstream]](https://github.com/microsoft/vscode-JSON.tmLanguage/commit/9bd83f1c252b375e957203f21793316203f61f70)
| Julia | file-extensions="jl" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/julia) [[upstream]](https://github.com/JuliaEditorSupport/atom-language-julia/commit/c686684f18153687886e7d19c1bfc3a33076b1ab)
| LaTeX | file-extensions="ctx, ltx, tex" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/latex) [[upstream]](https://github.com/jlelong/vscode-latex-basics/commit/7a35f5e0f19b28f5f1366579e2a9ad34df4f40c9)
| Less | file-extensions="less" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/less) [[upstream]](https://github.com/radium-v/Better-Less/commit/63c0cba9792e49e255cce0f6dd03250fb30591e6)
| Lua | file-extensions="lua" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/lua) [[upstream]](https://github.com/sumneko/lua.tmbundle/commit/1483add845ebfb3e1e631fe372603e5fed2cdd42)
| Makefile | file-extensions="mak, mk"<br />file-names="GNUmakefile, Makefile, OCamlMakefile, makefile" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/make) [[upstream]](https://github.com/fadeevab/make.tmbundle/commit/1d4c0b541959995db098df751ffc129da39a294b)
| Markdown <img src="syntaxes/markdown/icon.png" width=16/> | file-extensions="markdn, markdown, md, mdown, mdtext, mdtxt, mdwn, mkd, workbook" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/markdown-basics) [[upstream]](https://github.com/microsoft/vscode-markdown-tm-grammar/commit/7418dd20d76c72e82fadee2909e03239e9973b35)
| MS SQL | file-extensions="dsql, sql" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/sql) [[upstream]](https://github.com/microsoft/vscode-mssql/commit/49eff02f68b6ee73025c6665c672ca1c93385dde)
| Objective-C | file-extensions="m" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/objective-c) [[upstream]](https://github.com/jeff-hykin/better-objc-syntax/commit/119b75fb1f4d3e8726fa62588e3b935e0b719294)
| Objective-C++ | file-extensions="mm" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/objective-c) [[upstream]](https://github.com/jeff-hykin/better-objcpp-syntax/commit/5a7eb15eee382dd5aa388bc04fdb60a0d2128e14)
| Perl | file-extensions="PL, pl, pm, pod, psgi, t" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/perl) [[upstream]](https://github.com/textmate/perl.tmbundle/commit/a85927a902d6e5d7805f56a653f324d34dfad53a)
| PHP | file-extensions="ctp, php, php4, php5, phtml" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/php) [[upstream]](https://github.com/KapitanOczywisty/language-php/commit/5e8f000cb5a20f44f7a7a89d07ad0774031c53f3)
| PowerShell | file-extensions="ps1, psd1, psm1, psrc, pssc" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/powershell) [[upstream]](https://github.com/PowerShell/EditorSyntax/commit/742f0b5d4b60f5930c0b47fcc1f646860521296e)
| Pug | file-extensions="jade, pug" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/pug) [[upstream]](https://github.com/davidrios/pug-tmbundle/commit/ae1dd60ca4aa4b45617f236d584216cd8d19eecf)
| Python | file-extensions="cpy, gyp, gypi, ipy, py, pyi, pyt, pyw, rpy"<br />file-names="SConscript, SConstruct" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/python) [[upstream]](https://github.com/MagicStack/MagicPython/commit/7d0f2b22a5ad8fccbd7341bc7b7a715169283044)
| R | file-extensions="r, rhistory, rprofile, rt" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/r) [[upstream]](https://github.com/REditorSupport/vscode-R/commit/c937cdd39982995d8ee1f1125919f7c2d150b35d)
| Raku (Perl 6) | file-extensions="nqp, p6, pl6, pm6, raku, rakudoc, rakumod, rakutest" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/perl) [[upstream]](https://github.com/textmate/perl.tmbundle/commit/d9841a0878239fa43f88c640f8d458590f97e8f5)
| reStructuredText | file-extensions="rst" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/restructuredtext) [[upstream]](https://github.com/trond-snekvik/vscode-rst/commit/7f2d6bb4e20642b60f2979afcb594cfe4b48117a)
| Ruby | file-extensions="erb, gemspec, podspec, rake, rb, rbi, rbx, rjs, ru"<br />file-names="appfile, appraisals, berksfile, berksfile.lock, brewfile, capfile, cheffile, dangerfile, deliverfile, fastfile, gemfile, guardfile, gymfile, hobofile, matchfile, podfile, puppetfile, rakefile, rantfile, scanfile, snapfile, thorfile, vagrantfile" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/ruby) [[upstream]](https://github.com/Shopify/ruby-lsp/commit/01a8ecf608b7d8607adcd89c32db72ae3852f33b)
| Rust <img src="syntaxes/rust/rust.icon.png" width=16/> | file-extensions="rs" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/rust) [[upstream]](https://github.com/dustypomerleau/rust-syntax/commit/e90d3dbdb61b96e4afdce6f7a3572426b1a86d9d)
| SCSS | file-extensions="scss" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/scss) [[upstream]](https://github.com/atom/language-sass/commit/f52ab12f7f9346cc2568129d8c4419bd3d506b47)
| ShaderLab | file-extensions="shader" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/shaderlab) [[upstream]](https://github.com/tgjones/shaders-tmLanguage/commit/c72c8b39380ba5a86c58ceed053b5d965ebf38b3)
| Shell Script | file-extensions="Xsession, bash, bash_aliases, bash_login, bash_logout, bash_profile, bashrc, csh, cshrc, ebuild, eclass, fish, ksh, profile, sh, tcshrc, xprofile, xsession, xsessionrc, yash_profile, yashrc, zlogin, zlogout, zprofile, zsh, zsh-theme, zshenv, zshrc"<br />file-names=".envrc, .hushlogin, APKBUILD, PKGBUILD, bashrc_Apple_Terminal, zlogin, zlogout, zprofile, zshenv, zshrc, zshrc_Apple_Terminal"<br />file-patterns=".env.\*" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/shellscript) [[upstream]](https://github.com/jeff-hykin/better-shell-syntax/commit/35020b0bd79a90d3b262b4c13a8bb0b33adc1f45)
| Swift | file-extensions="swift" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/swift) [[upstream]](https://github.com/jtbandes/swift-tmlanguage/commit/b8d2889b4af1d8bad41578317a6adade642555a3)
| TeX | file-extensions="bbx, cbx, cls, sty" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/latex) [[upstream]](https://github.com/jlelong/vscode-latex-basics/commit/df6ef817c932d24da5cc72927344a547e463cc65)
| TypeScript | file-extensions="cts, mts, ts" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/typescript-basics) [[upstream]](https://github.com/microsoft/TypeScript-TmLanguage/commit/48f608692aa6d6ad7bd65b478187906c798234a8)
| TypeScript JSX | file-extensions="tsx" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/typescript-basics) [[upstream]](https://github.com/microsoft/TypeScript-TmLanguage/commit/48f608692aa6d6ad7bd65b478187906c798234a8)
| Visual Basic | file-extensions="bas, brs, vb, vba, vbs" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/vb) [[upstream]](https://github.com/textmate/asp.vb.net.tmbundle/commit/72d44550b3286d0382d7be0624140cf97857ff69)
| VS Code Search Result <img src="syntaxes/search-result/icon.png" width=16/> | file-extensions="code-search" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/search-result)
| XML <img src="syntaxes/xml/icon.png" width=16/> | file-extensions="ascx, atom, axaml, axml, bpmn, cpt, csl, csproj, csproj.user, dita, ditamap, dtd, dtml, ent, fsproj, fxml, iml, isml, jmx, launch, menu, mod, mxml, nuspec, opml, owl, proj, props, pt, publishsettings, pubxml, pubxml.user, rbxlx, rbxmx, rdf, rng, rss, shproj, storyboard, svg, targets, tld, tmx, vbproj, vbproj.user, vcxproj, vcxproj.filters, wsdl, wxi, wxl, wxs, xaml, xbl, xib, xlf, xliff, xml, xoml, xpdl, xsd, xul" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/xml) [[upstream]](https://github.com/atom/language-xml/commit/7bc75dfe779ad5b35d9bf4013d9181864358cb49)
| XSL <img src="syntaxes/xml/icon.png" width=16/> | file-extensions="xsl, xslt" | [1.98.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/ddc367ed5c8936efe395cffeec279b04ffd7db78/extensions/xml) [[upstream]](https://github.com/atom/language-xml/commit/507de2ee7daca60cf02e9e21fbeb92bbae73e280)
| YAML | file-extensions="cff, eyaml, eyml, winget, yaml, yaml-tmlanguage, yaml-tmpreferences, yaml-tmtheme, yml" | [1.96.1@microsoft/vscode](https://github.com/microsoft/vscode/tree/42b266171e51a016313f47d0c48aca9295b9cbb2/extensions/yaml) [[upstream]](https://github.com/textmate/yaml.tmbundle/commit/e54ceae3b719506dba7e481a77cea4a8b576ae46)

<!-- END-GENERATED -->


## <a name="how-to-update"></a>How to update grammars

To update the grammar files do:

1. Update version value under `sources/vscode-extensions/github/ref` setting in `updater/updater-config.yaml` to the desired value.

1. Run `updater/run.sh` or `updater/run.cmd`

   ```batch
   $ updater/run.sh
   Launching...
   Loading [X:\workspace\tm4e\org.eclipse.tm4e.language_pack\updater\updater-config.yaml]...
     -> 1 source repos defined
   Syntaxes Folder: [X:\workspace\tm4e\org.eclipse.tm4e.language_pack\syntaxes]
   Source Repos Cache Dir: [C:\Users\User1\AppData\Local\Temp\syntax-repos]
   Loading [X:\workspace\tm4e\org.eclipse.tm4e.language_pack\updater\updater-state.yaml]...
   ==================================================================
   [1/1] Processing [vscode-extensions] (VSCodeMultiExtensionsSource)
   ==================================================================
   Sparse checkout [https://github.com/microsoft/vscode]...
   Locating valid VSCode grammar extensions...
   Validating extension overrides...
     ==================================================================
     [1/49] Processing [bat] (VSCodeSingleExtensionSource)
     ==================================================================
     Validating language configuration overrides...OK
     Copying file [bat.tmLanguage.json]... OK -> 19288 bytes
     Copying file [bat.language-configuration.json]... OK -> 484 bytes
     ==================================================================
     [2/49] Processing [clojure] (VSCodeSingleExtensionSource)
     ==================================================================
     Copying file [clojure.tmLanguage.json]... OK -> 9632 bytes
     Copying file [clojure.language-configuration.json]... OK -> 373 bytes
     ...
   Saving state to [X:\workspace\tm4e\org.eclipse.tm4e.language_pack\updater\updater-state.yaml]
   ==================================================================
   Updating [plugin.xml]...
   ==================================================================
   Rendering entry [bat/bat]...
   Rendering entry [clojure/clojure]...
   ...
   Saving [X:\workspace\tm4e\org.eclipse.tm4e.language_pack\plugin.xml]...
   ==================================================================
   Updating [README.md.]...
   ==================================================================
   Saving [X:\workspace\tm4e\org.eclipse.tm4e.language_pack\README.md]...
   ==================================================================
   Updating [about.html.]...
   ==================================================================
   Saving [X:\workspace\tm4e\org.eclipse.tm4e.language_pack\about.html]...
   ========================================================
   **DONE**
   ========================================================
   ```
