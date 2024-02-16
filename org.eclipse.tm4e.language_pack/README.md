# Language Pack

1. [About](#about)
1. [Supported File Formats/Languages](#supported-fileformats)
1. [How to update grammars](#how-to-update)


## <a name="about"></a>About

Syntax highlighting and more for about 40 programming languages and file formats taken from [Visual Studio Code](https://github.com/microsoft/vscode/tree/main/extensions).

[TextMate grammars](https://en.wikipedia.org/wiki/TextMate#Language_Grammars) and
[language configurations](https://code.visualstudio.com/api/language-extensions/language-configuration-guide)
taken from [Visual Studio Code](https://github.com/microsoft/vscode/tree/main/extensions) and
provided as a plugin based on [Eclipse tm4e](https://github.com/eclipse/tm4e)
for the [Eclipse IDE](https://eclipseide.org).


## <a name="supported-fileformats"></a>Supported File Formats/Languages

<!-- START-GENERATED -->

| Language/Format | File Associations | Source
|:--------------- |:----------------- |:------ |
| ASP.NET Razor | file-extensions="cshtml, razor" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/razor) [[upstream]](https://github.com/dotnet/razor/commit/b44d0a906d054d2d343adc3f58cbea11d97d7488)
| Batch File <img src="syntaxes/bat/bat.icon.png" width=16/> | file-extensions="bat, cmd" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/bat) [[upstream]](https://github.com/mmims/language-batchfile/commit/6154ae25a24e01ac9329e7bcf958e093cd8733a9)
| BibTeX | file-extensions="bib" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/latex) [[upstream]](https://github.com/jlelong/vscode-latex-basics/commit/36411b38cf4ed18e02050249e2162b1316488686)
| C | file-extensions="c, i" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/cpp) [[upstream]](https://github.com/jeff-hykin/better-c-syntax/commit/34712a6106a4ffb0a04d2fa836fd28ff6c5849a4)
| C# | file-extensions="cake, cs, csx" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/csharp) [[upstream]](https://github.com/dotnet/csharp-tmLanguage/commit/6666eb1d5e6fb565a4110d6db645cc534fb3c6d2)
| C++ | file-extensions="c++, cc, cpp, cxx, h, h++, h.in, hh, hpp, hpp.in, hxx, ii, inl, ino, ipp, ixx, tpp, txx" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/cpp) [[upstream]](https://github.com/jeff-hykin/better-cpp-syntax/commit/f1d127a8af2b184db570345f0bb179503c47fdf6)
| Clojure <img src="syntaxes/clojure/clojure.icon.png" width=16/> | file-extensions="clj, cljc, cljs, cljx, clojure, edn" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/clojure) [[upstream]](https://github.com/atom/language-clojure/commit/45bdb881501d0b8f8b707ca1d3fcc8b4b99fca03)
| Code Snippets | file-extensions="code-snippets"<br />file-patterns="\*\*/User/profiles/\*/snippets/\*.json, \*\*/User/snippets/\*.json" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/json) [[upstream]](https://github.com/jeff-hykin/better-snippet-syntax/commit/2b1bb124cb2b9c75c3c80eae1b8f3a043841d654)
| CoffeeScript <img src="syntaxes/coffeescript/coffeescript.icon.png" width=16/> | file-extensions="coffee, cson, iced" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/coffeescript) [[upstream]](https://github.com/atom/language-coffee-script/commit/0f6db9143663e18b1ad00667820f46747dba495e)
| CSS | file-extensions="css" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/css) [[upstream]](https://github.com/microsoft/vscode-css/commit/c216f777497265700ff336f739328e5197e012cd)
| CUDA C++ | file-extensions="cu, cuh" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/cpp) [[upstream]](https://github.com/NVIDIA/cuda-cpp-grammar/commit/81e88eaec5170aa8585736c63627c73e3589998c)
| Dart | file-extensions="dart" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/dart) [[upstream]](https://github.com/dart-lang/dart-syntax-highlight/commit/bd0079ba7ca85cdd6400a8a467e532b539d0f285)
| Docker | file-extensions="containerfile, dockerfile"<br />file-names="Containerfile, Dockerfile"<br />file-patterns="Containerfile.\*, Dockerfile.\*" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/docker) [[upstream]](https://github.com/moby/moby/commit/abd39744c6f3ed854500e423f5fabf952165161f)
| F# | file-extensions="fs, fsi, fsscript, fsx" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/fsharp) [[upstream]](https://github.com/ionide/ionide-fsgrammar/commit/078bbf85225b82de3ae4c2db7ee74d9239b249ce)
| Git Commit Message <img src="syntaxes/git-base/icon.png" width=16/> | file-names="COMMIT_EDITMSG, MERGE_MSG" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/git-base) [[upstream]](https://github.com/walles/git-commit-message-plus/commit/35a079dea5a91b087021b40c01a6bb4eb0337a87)
| Git Ignore <img src="syntaxes/git-base/icon.png" width=16/> | file-extensions="git-blame-ignore-revs, gitignore, gitignore_global"<br />file-names=".gitignore" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/git-base)
| Git Rebase Message <img src="syntaxes/git-base/icon.png" width=16/> | file-names="git-rebase-todo"<br />file-patterns="\*\*/rebase-merge/done" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/git-base) [[upstream]](https://github.com/textmate/git.tmbundle/commit/5870cf3f8abad3a6637bdf69250b5d2ded427dc4)
| Go | file-extensions="go" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/go) [[upstream]](https://github.com/jeff-hykin/better-go-syntax/commit/6175663a7a0e23d58ccf9aab95054cb6e5c92aff)
| Groovy | file-extensions="gradle, groovy, gvy, jenkinsfile, nf"<br />file-names="Jenkinsfile"<br />file-patterns="Jenkinsfile\*" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/groovy) [[upstream]](https://github.com/textmate/groovy.tmbundle/commit/85d8f7c97ae473ccb9473f6c8d27e4ec957f4be1)
| Handlebars | file-extensions="handlebars, hbs, hjs" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/handlebars) [[upstream]](https://github.com/daaain/Handlebars/commit/85a153a6f759df4e8da7533e1b3651f007867c51)
| HLSL (High-Level Shader Language) | file-extensions="cginc, compute, fx, fxh, hlsl, hlsli, psh, vsh" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/hlsl) [[upstream]](https://github.com/tgjones/shaders-tmLanguage/commit/87c0dca3a39170dbd7ee7e277db4f915fb2de14a)
| HTML | file-extensions="asp, aspx, ejs, htm, html, jshtm, jsp, mdoc, rhtml, shtml, volt, xht, xhtml" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/html) [[upstream]](https://github.com/textmate/html.tmbundle/commit/390c8870273a2ae80244dae6db6ba064a802f407)
| INI Config File | file-extensions="ini" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/ini) [[upstream]](https://github.com/textmate/ini.tmbundle/commit/2af0cbb0704940f967152616f2f1ff0aae6287a6)
| Java <img src="syntaxes/java/java.icon.png" width=16/> | file-extensions="jav, java" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/java) [[upstream]](https://github.com/redhat-developer/vscode-java/commit/5d224a552cf5f0f8ebccf69e43e2575ed2c13839)
| JavaScript | file-extensions="cjs, es6, js, mjs, pac"<br />file-names="jakefile" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/javascript) [[upstream]](https://github.com/microsoft/TypeScript-TmLanguage/commit/8c7482b94b548eab56da64dbfb30b82589b3f747)
| JavaScript JSX | file-extensions="jsx" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/javascript) [[upstream]](https://github.com/microsoft/TypeScript-TmLanguage/commit/8c7482b94b548eab56da64dbfb30b82589b3f747)
| JSON | file-extensions="bowerrc, css.map, geojson, har, ipynb, js.map, jscsrc, jslintrc, json, jsonld, ts.map, vuerc, webmanifest"<br />file-names=".watchmanconfig, composer.lock" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/json) [[upstream]](https://github.com/microsoft/vscode-JSON.tmLanguage/commit/9bd83f1c252b375e957203f21793316203f61f70)
| JSON Lines | file-extensions="jsonl" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/json) [[upstream]](https://github.com/microsoft/vscode-JSON.tmLanguage/commit/9bd83f1c252b375e957203f21793316203f61f70)
| JSON with Comments | file-extensions="babelrc, eslintrc, eslintrc.json, hintrc, jsfmtrc, jshintrc, jsonc, swcrc"<br />file-names=".babelrc.json, .ember-cli, babel.config.json, typedoc.json" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/json) [[upstream]](https://github.com/microsoft/vscode-JSON.tmLanguage/commit/9bd83f1c252b375e957203f21793316203f61f70)
| Julia | file-extensions="jl" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/julia) [[upstream]](https://github.com/JuliaEditorSupport/atom-language-julia/commit/85cf3ef2ddcb7bc9b2b17b2a5f1fc664e1f1e718)
| LaTeX | file-extensions="ctx, ltx, tex" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/latex) [[upstream]](https://github.com/jlelong/vscode-latex-basics/commit/221a2443a5e8ac029685a0f872af39b23cc2b634)
| Less | file-extensions="less" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/less) [[upstream]](https://github.com/atom/language-less/commit/87d4d59e8de6796b506b81a16e1dc1fafc99d30f)
| Lua | file-extensions="lua" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/lua) [[upstream]](https://github.com/sumneko/lua.tmbundle/commit/3a18700941737c3ab66ac5964696f141aee61800)
| Makefile | file-extensions="mak, mk"<br />file-names="GNUmakefile, Makefile, OCamlMakefile, makefile" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/make) [[upstream]](https://github.com/fadeevab/make.tmbundle/commit/1d4c0b541959995db098df751ffc129da39a294b)
| Markdown <img src="syntaxes/markdown/icon.png" width=16/> | file-extensions="markdn, markdown, md, mdown, mdtext, mdtxt, mdwn, mkd, workbook" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/markdown-basics) [[upstream]](https://github.com/microsoft/vscode-markdown-tm-grammar/commit/0a4b23580308fdcfb4ab7b526e3e13ba17d436fb)
| Objective-C | file-extensions="m" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/objective-c) [[upstream]](https://github.com/jeff-hykin/better-objc-syntax/commit/119b75fb1f4d3e8726fa62588e3b935e0b719294)
| Objective-C++ | file-extensions="mm" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/objective-c) [[upstream]](https://github.com/jeff-hykin/better-objcpp-syntax/commit/5a7eb15eee382dd5aa388bc04fdb60a0d2128e14)
| Perl | file-extensions="PL, pl, pm, pod, psgi, t" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/perl) [[upstream]](https://github.com/textmate/perl.tmbundle/commit/a85927a902d6e5d7805f56a653f324d34dfad53a)
| PHP | file-extensions="ctp, php, php4, php5, phtml" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/php) [[upstream]](https://github.com/KapitanOczywisty/language-php/commit/5e8f000cb5a20f44f7a7a89d07ad0774031c53f3)
| PowerShell | file-extensions="ps1, psd1, psm1, psrc, pssc" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/powershell) [[upstream]](https://github.com/PowerShell/EditorSyntax/commit/742f0b5d4b60f5930c0b47fcc1f646860521296e)
| Pug | file-extensions="jade, pug" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/pug) [[upstream]](https://github.com/davidrios/pug-tmbundle/commit/ae1dd60ca4aa4b45617f236d584216cd8d19eecf)
| Python | file-extensions="cpy, gyp, gypi, ipy, py, pyi, pyt, pyw, rpy"<br />file-names="SConscript, SConstruct" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/python) [[upstream]](https://github.com/MagicStack/MagicPython/commit/7d0f2b22a5ad8fccbd7341bc7b7a715169283044)
| R | file-extensions="r, rhistory, rprofile, rt" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/r) [[upstream]](https://github.com/REditorSupport/vscode-R/commit/ad966f3b2de8a83594f90aa5b51f5b569cf02c08)
| Raku (Perl 6) | file-extensions="nqp, p6, pl6, pm6, raku, rakudoc, rakumod, rakutest" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/perl) [[upstream]](https://github.com/textmate/perl.tmbundle/commit/d9841a0878239fa43f88c640f8d458590f97e8f5)
| reStructuredText | file-extensions="rst" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/restructuredtext) [[upstream]](https://github.com/trond-snekvik/vscode-rst/commit/7f2d6bb4e20642b60f2979afcb594cfe4b48117a)
| Ruby | file-extensions="erb, gemspec, podspec, rake, rb, rbi, rbx, rjs, ru"<br />file-names="appfile, appraisals, berksfile, berksfile.lock, brewfile, capfile, cheffile, dangerfile, deliverfile, fastfile, gemfile, guardfile, gymfile, hobofile, matchfile, podfile, puppetfile, rakefile, rantfile, scanfile, snapfile, thorfile, vagrantfile" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/ruby) [[upstream]](https://github.com/textmate/ruby.tmbundle/commit/efcb8941c701343f1b2e9fb105c678152fea6892)
| Rust <img src="syntaxes/rust/rust.icon.png" width=16/> | file-extensions="rs" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/rust) [[upstream]](https://github.com/dustypomerleau/rust-syntax/commit/20730dff3c367cb40a7edd278fdaf0239ea50833)
| SCSS | file-extensions="scss" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/scss) [[upstream]](https://github.com/atom/language-sass/commit/f52ab12f7f9346cc2568129d8c4419bd3d506b47)
| ShaderLab | file-extensions="shader" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/shaderlab) [[upstream]](https://github.com/tgjones/shaders-tmLanguage/commit/c72c8b39380ba5a86c58ceed053b5d965ebf38b3)
| Shell Script | file-extensions="Xsession, bash, bash_aliases, bash_login, bash_logout, bash_profile, bashrc, csh, cshrc, ebuild, fish, ksh, profile, sh, tcshrc, xprofile, xsession, xsessionrc, yash_profile, yashrc, zlogin, zlogout, zprofile, zsh, zsh-theme, zshenv, zshrc"<br />file-names=".envrc, .hushlogin, APKBUILD, PKGBUILD, bashrc_Apple_Terminal, zlogin, zlogout, zprofile, zshenv, zshrc, zshrc_Apple_Terminal"<br />file-patterns=".env.\*" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/shellscript) [[upstream]](https://github.com/jeff-hykin/better-shell-syntax/commit/a3de7b32f1537194a83ee848838402fbf4b67424)
| SQL | file-extensions="dsql, sql" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/sql) [[upstream]](https://github.com/microsoft/vscode-mssql/commit/9cb3529a978ddf599bf5bdd228f21bbcfe2914f5)
| Swift | file-extensions="swift" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/swift) [[upstream]](https://github.com/jtbandes/swift-tmlanguage/commit/806e024d8ea2478596d7e0ab871d2b1436e6fead)
| TeX | file-extensions="bbx, cbx, cls, sty" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/latex) [[upstream]](https://github.com/jlelong/vscode-latex-basics/commit/3ae82b457c28f0368cbbb47024b0245ef1ff3d33)
| TypeScript | file-extensions="cts, mts, ts" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/typescript-basics) [[upstream]](https://github.com/microsoft/TypeScript-TmLanguage/commit/8c7482b94b548eab56da64dbfb30b82589b3f747)
| TypeScript JSX | file-extensions="tsx" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/typescript-basics) [[upstream]](https://github.com/microsoft/TypeScript-TmLanguage/commit/8c7482b94b548eab56da64dbfb30b82589b3f747)
| Visual Basic | file-extensions="bas, brs, vb, vba, vbs" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/vb) [[upstream]](https://github.com/textmate/asp.vb.net.tmbundle/commit/72d44550b3286d0382d7be0624140cf97857ff69)
| VS Code Search Result <img src="syntaxes/search-result/icon.png" width=16/> | file-extensions="code-search" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/search-result)
| XML <img src="syntaxes/xml/icon.png" width=16/> | file-extensions="ascx, atom, axaml, axml, bpmn, cpt, csl, csproj, csproj.user, dita, ditamap, dtd, dtml, ent, fsproj, fxml, iml, isml, jmx, launch, menu, mod, mxml, nuspec, opml, owl, proj, props, pt, publishsettings, pubxml, pubxml.user, rbxlx, rbxmx, rdf, rng, rss, shproj, storyboard, svg, targets, tld, tmx, vbproj, vbproj.user, vcxproj, vcxproj.filters, wsdl, wxi, wxl, wxs, xaml, xbl, xib, xlf, xliff, xml, xoml, xpdl, xsd, xul" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/xml) [[upstream]](https://github.com/atom/language-xml/commit/7bc75dfe779ad5b35d9bf4013d9181864358cb49)
| XSL <img src="syntaxes/xml/icon.png" width=16/> | file-extensions="xsl, xslt" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/xml) [[upstream]](https://github.com/atom/language-xml/commit/507de2ee7daca60cf02e9e21fbeb92bbae73e280)
| YAML | file-extensions="cff, eyaml, eyml, yaml, yaml-tmlanguage, yaml-tmpreferences, yaml-tmtheme, yml" | [1.85.2@microsoft/vscode](https://github.com/microsoft/vscode/tree/8b3775030ed1a69b13e4f4c628c612102e30a681/extensions/yaml) [[upstream]](https://github.com/textmate/yaml.tmbundle/commit/e54ceae3b719506dba7e481a77cea4a8b576ae46)

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
