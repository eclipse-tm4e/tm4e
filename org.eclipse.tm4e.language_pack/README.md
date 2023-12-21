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
| ASP.NET Razor | file-extensions="cshtml, razor" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/razor) [[upstream]](https://github.com/dotnet/razor/commit/8d0ae9664cb27276eab36d83e48e88356468ca67)
| Batch File <img src="syntaxes/bat/bat.png" width=16/> | file-extensions="bat, cmd" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/bat) [[upstream]](https://github.com/mmims/language-batchfile/commit/6154ae25a24e01ac9329e7bcf958e093cd8733a9)
| BibTeX | file-extensions="bib" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/latex) [[upstream]](https://github.com/jlelong/vscode-latex-basics/commit/4b19be579cb4a3c680f8b4bb613dcebfac826f8b)
| C | file-extensions="c, i" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/cpp) [[upstream]](https://github.com/jeff-hykin/better-c-syntax/commit/34712a6106a4ffb0a04d2fa836fd28ff6c5849a4)
| C# | file-extensions="cake, cs, csx" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/csharp) [[upstream]](https://github.com/dotnet/csharp-tmLanguage/commit/5e7dd90d2af9817b0dfb614b21c79a3e81882d9f)
| C++ | file-extensions="c++, cc, cpp, cxx, h, h++, h.in, hh, hpp, hpp.in, hxx, ii, inl, ino, ipp, ixx, tpp, txx" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/cpp) [[upstream]](https://github.com/jeff-hykin/better-cpp-syntax/commit/f1d127a8af2b184db570345f0bb179503c47fdf6)
| Clojure <img src="syntaxes/clojure/clojure.png" width=16/> | file-extensions="clj, cljc, cljs, cljx, clojure, edn" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/clojure) [[upstream]](https://github.com/atom/language-clojure/commit/45bdb881501d0b8f8b707ca1d3fcc8b4b99fca03)
| CoffeeScript <img src="syntaxes/coffeescript/coffeescript.png" width=16/> | file-extensions="coffee, cson, iced" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/coffeescript) [[upstream]](https://github.com/atom/language-coffee-script/commit/0f6db9143663e18b1ad00667820f46747dba495e)
| CSS | file-extensions="css" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/css) [[upstream]](https://github.com/microsoft/vscode-css/commit/1452547185a1793c946cf67f8c7c9001716e32c3)
| CUDA C++ | file-extensions="cu, cuh" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/cpp) [[upstream]](https://github.com/NVIDIA/cuda-cpp-grammar/commit/81e88eaec5170aa8585736c63627c73e3589998c)
| Dart | file-extensions="dart" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/dart) [[upstream]](https://github.com/dart-lang/dart-syntax-highlight/commit/b6324bfae2058bc29bb01af6bd2011c3eb35545d)
| Docker | file-extensions="containerfile, dockerfile"<br />file-names="Containerfile, Dockerfile"<br />file-patterns="Containerfile.\*, Dockerfile.\*" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/docker) [[upstream]](https://github.com/moby/moby/commit/abd39744c6f3ed854500e423f5fabf952165161f)
| F# | file-extensions="fs, fsi, fsscript, fsx" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/fsharp) [[upstream]](https://github.com/ionide/ionide-fsgrammar/commit/713cd4a34e7729e444cf85ae287dd94c19e34337)
| Git Commit Message <img src="syntaxes/git-base/icon.png" width=16/> | file-names="COMMIT_EDITMSG, MERGE_MSG" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/git-base) [[upstream]](https://github.com/walles/git-commit-message-plus/commit/35a079dea5a91b087021b40c01a6bb4eb0337a87)
| Git Ignore <img src="syntaxes/git-base/icon.png" width=16/> | file-extensions="gitignore, gitignore_global"<br />file-names=".gitignore" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/git-base)
| Git Rebase Message <img src="syntaxes/git-base/icon.png" width=16/> | file-names="git-rebase-todo"<br />file-patterns="\*\*/rebase-merge/done" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/git-base) [[upstream]](https://github.com/textmate/git.tmbundle/commit/5870cf3f8abad3a6637bdf69250b5d2ded427dc4)
| Go | file-extensions="go" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/go) [[upstream]](https://github.com/jeff-hykin/better-go-syntax/commit/6175663a7a0e23d58ccf9aab95054cb6e5c92aff)
| Groovy | file-extensions="gradle, groovy, gvy, jenkinsfile, nf"<br />file-names="Jenkinsfile"<br />file-patterns="Jenkinsfile\*" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/groovy) [[upstream]](https://github.com/textmate/groovy.tmbundle/commit/85d8f7c97ae473ccb9473f6c8d27e4ec957f4be1)
| Handlebars | file-extensions="handlebars, hbs, hjs" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/handlebars) [[upstream]](https://github.com/daaain/Handlebars/commit/85a153a6f759df4e8da7533e1b3651f007867c51)
| HLSL (High-Level Shader Language) | file-extensions="cginc, compute, fx, fxh, hlsl, hlsli, psh, vsh" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/hlsl) [[upstream]](https://github.com/tgjones/shaders-tmLanguage/commit/87c0dca3a39170dbd7ee7e277db4f915fb2de14a)
| HTML | file-extensions="asp, aspx, ejs, htm, html, jshtm, jsp, mdoc, rhtml, shtml, volt, xht, xhtml" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/html) [[upstream]](https://github.com/textmate/html.tmbundle/commit/390c8870273a2ae80244dae6db6ba064a802f407)
| INI Config File | file-extensions="ini" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/ini) [[upstream]](https://github.com/textmate/ini.tmbundle/commit/2af0cbb0704940f967152616f2f1ff0aae6287a6)
| Java | file-extensions="jav, java" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/java) [[upstream]](https://github.com/atom/language-java/commit/29f977dc42a7e2568b39bb6fb34c4ef108eb59b3)
| JavaScript | file-extensions="cjs, es6, js, mjs, pac"<br />file-names="jakefile" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/javascript) [[upstream]](https://github.com/microsoft/TypeScript-TmLanguage/commit/e0aefd8205cc9d1bc7859cc5babbee0d833dca0f)
| JavaScript JSX | file-extensions="jsx" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/javascript) [[upstream]](https://github.com/microsoft/TypeScript-TmLanguage/commit/e0aefd8205cc9d1bc7859cc5babbee0d833dca0f)
| JSON | file-extensions="bowerrc, css.map, geojson, har, ipynb, js.map, jscsrc, jslintrc, json, jsonld, ts.map, vuerc, webmanifest"<br />file-names=".watchmanconfig, composer.lock" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/json) [[upstream]](https://github.com/microsoft/vscode-JSON.tmLanguage/commit/9bd83f1c252b375e957203f21793316203f61f70)
| JSON Lines | file-extensions="jsonl" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/json) [[upstream]](https://github.com/microsoft/vscode-JSON.tmLanguage/commit/9bd83f1c252b375e957203f21793316203f61f70)
| JSON with Comments | file-extensions="babelrc, eslintrc, eslintrc.json, hintrc, jsfmtrc, jshintrc, jsonc, swcrc"<br />file-names=".babelrc.json, .ember-cli, babel.config.json, typedoc.json" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/json) [[upstream]](https://github.com/microsoft/vscode-JSON.tmLanguage/commit/9bd83f1c252b375e957203f21793316203f61f70)
| Julia | file-extensions="jl" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/julia) [[upstream]](https://github.com/JuliaEditorSupport/atom-language-julia/commit/7b7801f41ce4ac1303bd17e057dbe677e24f597f)
| Less | file-extensions="less" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/less) [[upstream]](https://github.com/atom/language-less/commit/87d4d59e8de6796b506b81a16e1dc1fafc99d30f)
| Lua | file-extensions="lua" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/lua) [[upstream]](https://github.com/sumneko/lua.tmbundle/commit/3a18700941737c3ab66ac5964696f141aee61800)
| Makefile | file-extensions="mak, mk"<br />file-names="GNUmakefile, Makefile, OCamlMakefile, makefile" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/make) [[upstream]](https://github.com/fadeevab/make.tmbundle/commit/1d4c0b541959995db098df751ffc129da39a294b)
| Markdown | file-extensions="markdn, markdown, md, mdown, mdtext, mdtxt, mdwn, mkd, workbook" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/markdown-basics) [[upstream]](https://github.com/microsoft/vscode-markdown-tm-grammar/commit/ca2caf2157d0674be3d641f71499b84d514e4e5e)
| Objective-C | file-extensions="m" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/objective-c) [[upstream]](https://github.com/jeff-hykin/better-objc-syntax/commit/119b75fb1f4d3e8726fa62588e3b935e0b719294)
| Objective-C++ | file-extensions="mm" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/objective-c) [[upstream]](https://github.com/jeff-hykin/better-objcpp-syntax/commit/5a7eb15eee382dd5aa388bc04fdb60a0d2128e14)
| Perl | file-extensions="PL, pl, pm, pod, psgi, t" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/perl) [[upstream]](https://github.com/textmate/perl.tmbundle/commit/a85927a902d6e5d7805f56a653f324d34dfad53a)
| Perl 6 (Raku) | file-extensions="nqp, p6, pl6, pm6" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/perl) [[upstream]](https://github.com/textmate/perl.tmbundle/commit/d9841a0878239fa43f88c640f8d458590f97e8f5)
| PowerShell | file-extensions="ps1, psd1, psm1, psrc, pssc" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/powershell) [[upstream]](https://github.com/PowerShell/EditorSyntax/commit/742f0b5d4b60f5930c0b47fcc1f646860521296e)
| Pug | file-extensions="jade, pug" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/pug) [[upstream]](https://github.com/davidrios/pug-tmbundle/commit/ae1dd60ca4aa4b45617f236d584216cd8d19eecf)
| Python | file-extensions="cpy, gyp, gypi, ipy, py, pyi, pyt, pyw, rpy"<br />file-names="SConscript, SConstruct" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/python) [[upstream]](https://github.com/MagicStack/MagicPython/commit/7d0f2b22a5ad8fccbd7341bc7b7a715169283044)
| R | file-extensions="r, rhistory, rprofile, rt" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/r) [[upstream]](https://github.com/Ikuyadeu/vscode-R/commit/ff60e426f66503f3c9533c7a62a8fd3f9f6c53df)
| reStructuredText | file-extensions="rst" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/restructuredtext) [[upstream]](https://github.com/trond-snekvik/vscode-rst/commit/4f6f1a8f94e0d16e30dddc9c4e359d062b715408)
| Rust <img src="syntaxes/rust/rust.png" width=16/> | file-extensions="rs" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/rust) [[upstream]](https://github.com/dustypomerleau/rust-syntax/commit/20462d50ff97338f42c6b64c3f421c634fd60734)
| SCSS | file-extensions="scss" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/scss) [[upstream]](https://github.com/atom/language-sass/commit/f52ab12f7f9346cc2568129d8c4419bd3d506b47)
| ShaderLab | file-extensions="shader" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/shaderlab) [[upstream]](https://github.com/tgjones/shaders-tmLanguage/commit/c72c8b39380ba5a86c58ceed053b5d965ebf38b3)
| Shell Script | file-extensions="Xsession, bash, bash_aliases, bash_login, bash_logout, bash_profile, bashrc, csh, cshrc, ebuild, fish, ksh, profile, sh, tcshrc, xprofile, xsession, xsessionrc, yash_profile, yashrc, zlogin, zlogout, zprofile, zsh, zsh-theme, zshenv, zshrc"<br />file-names=".envrc, .hushlogin, APKBUILD, PKGBUILD, bashrc_Apple_Terminal, zlogin, zlogout, zprofile, zshenv, zshrc, zshrc_Apple_Terminal"<br />file-patterns=".env.\*" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/shellscript) [[upstream]](https://github.com/jeff-hykin/better-shell-syntax/commit/1bad17d8badf6283125aaa7c31be06ba64146a0f)
| SQL | file-extensions="dsql, sql" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/sql) [[upstream]](https://github.com/microsoft/vscode-mssql/commit/3929516cce0a570e91ee1be74b09ed886cb360f4)
| Swift | file-extensions="swift" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/swift) [[upstream]](https://github.com/textmate/swift.tmbundle/commit/7a35637eb70aef3114b091c4ff6fbf6a2faa881b)
| TeX | file-extensions="bbx, cbx, cls, sty" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/latex) [[upstream]](https://github.com/jlelong/vscode-latex-basics/commit/2be145a0bde15dfaf367676a1227c8a701792f90)
| TypeScript | file-extensions="cts, mts, ts" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/typescript-basics) [[upstream]](https://github.com/microsoft/TypeScript-TmLanguage/commit/e0aefd8205cc9d1bc7859cc5babbee0d833dca0f)
| TypeScript JSX | file-extensions="tsx" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/typescript-basics) [[upstream]](https://github.com/microsoft/TypeScript-TmLanguage/commit/e0aefd8205cc9d1bc7859cc5babbee0d833dca0f)
| Visual Basic | file-extensions="bas, brs, vb, vba, vbs" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/vb) [[upstream]](https://github.com/textmate/asp.vb.net.tmbundle/commit/72d44550b3286d0382d7be0624140cf97857ff69)
| XML | file-extensions="ascx, atom, axaml, axml, bpmn, cpt, csl, csproj, csproj.user, dita, ditamap, dtd, dtml, ent, fsproj, fxml, iml, isml, jmx, launch, menu, mod, mxml, nuspec, opml, owl, proj, props, pt, publishsettings, pubxml, pubxml.user, rbxlx, rbxmx, rdf, rng, rss, shproj, storyboard, svg, targets, tld, tmx, vbproj, vbproj.user, vcxproj, vcxproj.filters, wsdl, wxi, wxl, wxs, xaml, xbl, xib, xlf, xliff, xml, xoml, xpdl, xsd, xul" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/xml) [[upstream]](https://github.com/atom/language-xml/commit/7bc75dfe779ad5b35d9bf4013d9181864358cb49)
| XSL | file-extensions="xsl, xslt" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/xml) [[upstream]](https://github.com/atom/language-xml/commit/507de2ee7daca60cf02e9e21fbeb92bbae73e280)
| YAML | file-extensions="cff, eyaml, eyml, yaml, yml" | [1.79.0@microsoft/vscode](https://github.com/microsoft/vscode/tree/b380da4ef1ee00e224a15c1d4d9793e27c2b6302/extensions/yaml) [[upstream]](https://github.com/textmate/yaml.tmbundle/commit/e54ceae3b719506dba7e481a77cea4a8b576ae46)

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
