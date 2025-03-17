package com.example.botchat.chatbox.viewModel.text

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle

object TextProcessor {

    fun extractUrlsFromText(text: String): List<String> {
        val regexUrl = "(https?://[\\w-]+(\\.[\\w-]+)+(/[\\w-./?%&=@]*)?)".toRegex()
        return regexUrl.findAll(text).map { it.value }.toList()
    }

    fun splitTextByCode(input: String): List<String> {
        val regex = "```(.*?)```".toRegex(RegexOption.DOT_MATCHES_ALL)
        val splitText = mutableListOf<String>()
        var lastEndIndex = 0

        regex.findAll(input).forEach { match ->
            val beforeCode = input.substring(lastEndIndex, match.range.first)
            if (beforeCode.isNotEmpty()) splitText.add(beforeCode)
            splitText.add(match.value)
            lastEndIndex = match.range.last + 1
        }

        if (lastEndIndex < input.length) {
            splitText.add(input.substring(lastEndIndex))
        }
        return splitText
    }

    fun splitCodeHeaderAndBody(code: String): Pair<String, String> {
        val content = code.removePrefix("```").removeSuffix("```").trim()
        val lines = content.split("\n")
        val header = lines.firstOrNull() ?: ""
        val body = lines.drop(1).joinToString("\n")
        return header to body
    }

    fun buildAnnotatedText(
        text: String,
        linkColor: Color,
        boldColor: Color,
        codeInlineColor: Color
    ): AnnotatedString {
        return buildAnnotatedString {
            val regexUrl = "(https?://[\\w-]+(\\.[\\w-]+)+(/[\\w-./?%&=@]*)?)".toRegex()
            val regexBold = "\\*\\*(.*?)(:?)\\*\\*".toRegex()
            val regexCode = "`(.*?)`".toRegex()
            var lastIndex = 0

            regexUrl.findAll(text).forEach { match ->
                append(text.substring(lastIndex, match.range.first))
                val url = match.value
                pushStringAnnotation(tag = "URL", annotation = url)
                withStyle(
                    style = SpanStyle(
                        color = linkColor,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append(url)
                }
                pop()
                lastIndex = match.range.last + 1
            }

            val remainingText = text.substring(lastIndex)
            var innerLastIndex = 0
            regexBold.findAll(remainingText).forEach { match ->
                append(remainingText.substring(innerLastIndex, match.range.first))
                val boldContent = match.groupValues[1]
                val trailingColon = match.groupValues[2]
                val innerText = buildAnnotatedString {
                    var codeLastIndex = 0
                    regexCode.findAll(boldContent).forEach { codeMatch ->
                        append(boldContent.substring(codeLastIndex, codeMatch.range.first))
                        val codeText = codeMatch.groupValues[1]
                        withStyle(style = SpanStyle(color = codeInlineColor)) {
                            append(codeText)
                        }
                        codeLastIndex = codeMatch.range.last + 1
                    }
                    if (codeLastIndex < boldContent.length) {
                        append(boldContent.substring(codeLastIndex))
                    }
                }
                withStyle(
                    style = SpanStyle(
                        color = boldColor,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                ) {
                    append(innerText)
                }
                if (trailingColon.isNotEmpty()) {
                    append(trailingColon)
                }
                innerLastIndex = match.range.last + 1
            }

            if (innerLastIndex < remainingText.length) {
                append(remainingText.substring(innerLastIndex))
            }
        }
    }

    fun filterSpecialCharacters(input: String): String {
        val regex = "[~!^*?|\\[\\]\\\\`\"-]".toRegex()
        return input.replace(regex, "")
    }

    fun splitTextByTable(input: String): List<TableOrTextBlock> {
        // Regex đơn giản bắt đầu từ dòng `| ... |` + kế tiếp là dòng `| --- |...|` + >=1 dòng body
        val tableRegex = Regex(
            "(?s)(\\|[^\\n]+\\|\\s*\\n\\|[\\-\\s\\|]+\\|\\s*\\n(?:\\|.*?\\|\\s*\\n)+)"
        )

        val result = mutableListOf<TableOrTextBlock>()
        var lastIndex = 0

        tableRegex.findAll(input).forEach { match ->
            // Lấy đoạn text thường trước bảng
            if (match.range.first > lastIndex) {
                val beforeText = input.substring(lastIndex, match.range.first)
                if (beforeText.isNotEmpty()) {
                    result.add(TableOrTextBlock.TextBlock(beforeText))
                }
            }
            // Bảng Markdown
            val tableText = match.value
            val tableData = parseMarkdownTable(tableText)
            result.add(TableOrTextBlock.TableBlock(tableData))
            lastIndex = match.range.last + 1
        }

        // Phần text còn lại (nếu có) sau cùng
        if (lastIndex < input.length) {
            val trailingText = input.substring(lastIndex)
            if (trailingText.isNotEmpty()) {
                result.add(TableOrTextBlock.TextBlock(trailingText))
            }
        }
        return result
    }

    /**
     *  Hàm parse 1 khối bảng Markdown thành list 2D (mảng các dòng, mỗi dòng là list ô).
     *  Đồng thời loại bỏ các kí tự `|` và `-`.
     */
    private fun parseMarkdownTable(tableText: String): List<List<String>> {
        val lines = tableText.trim().lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.size < 2) return emptyList()

        val headerColumns = lines.first().split("|").map { it.trim() }.filter { it.isNotEmpty() }
        val numColumns = headerColumns.size

        val pureLines = lines.filterNot { it.matches(Regex("\\|?\\s*[-]+\\s*\\|?")) }

        val table = pureLines.map { line ->
            line.split("|").map { it.trim().replace("[|\\-]".toRegex(), "") }
                .filter { it.isNotEmpty() }
                .let {
                    // Đảm bảo luôn đúng số lượng cột
                    if (it.size < numColumns) it + List(numColumns - it.size) { "" } else it
                }
        }

        return table
    }


    // 2 loại khối khác nhau: Bảng hoặc Text thường
    sealed class TableOrTextBlock {
        data class TableBlock(val table: List<List<String>>) : TableOrTextBlock()
        data class TextBlock(val text: String) : TableOrTextBlock()
    }

    fun highlightCode(header: String, code: String, isDarkTheme: Boolean = true): AnnotatedString {
        val language = header.trim().lowercase()
        val defaultColor = if (isDarkTheme) Color(0xFFD6D4D4) else Color(0xFF161616)

        return buildAnnotatedString {
            val keywords = getKeywordsForLanguage(language)
            val types = getTypesForLanguage(language)
            var currentIndex = 0

            // Regex cải tiến, tách biệt # comment và # ký tự đặc biệt
            val tokenRegex = Regex(
                """(\w+)|("[^"]*"|'[^']*')|(//.*?$)|(#\s.*?$)|(/\*.*?\*/)|(\<!--.*?--\>)|([{}()\[\];,.+\-*/=<>!&|])|(\d*\.?\d+)|([@#$%^](?![^\s#].*?$)|\#[0-9A-Fa-f]{3,6})""",
                setOf(RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL)
            )

            tokenRegex.findAll(code).forEach { match ->
                if (currentIndex < match.range.first) {
                    append(code.substring(currentIndex, match.range.first)) // Không tô màu khoảng trắng
                }

                val token = match.value
                when {
                    // Xử lý comment dòng đơn (//) cho các ngôn ngữ không phải Python
                    token.startsWith("//") && language != "python" -> {
                        withStyle(style = SpanStyle(color = Color(0xFF868686))) { // Xám nhạt
                            append(token)
                        }
                    }
                    // Xử lý comment Python (#), toàn bộ dòng từ # đến cuối nếu bắt đầu bằng # và có khoảng trắng
                    token.startsWith("#") && language == "python" && token.matches(Regex("""#\s.*""")) -> {
                        withStyle(style = SpanStyle(color = Color(0xFF868686))) { // Xám nhạt
                            append(token)
                        }
                    }
                    // Xử lý comment khối (/* ... */)
                    token.startsWith("/*") && token.endsWith("*/") -> {
                        withStyle(style = SpanStyle(color = Color(0xFF868686))) {
                            append(token)
                        }
                    }
                    // Xử lý comment HTML (<!-- ... -->)
                    token.startsWith("<!--") && token.endsWith("-->") -> {
                        withStyle(style = SpanStyle(color = Color(0xFF868686))) {
                            append(token)
                        }
                    }
                    // Xử lý chuỗi (String)
                    token.startsWith("\"") || token.startsWith("'") -> {
                        withStyle(style = SpanStyle(color = Color(0xFFD69D85))) { // Cam nhạt
                            append(token)
                        }
                    }
                    // Xử lý số
                    token.matches(Regex("""\d*\.?\d+""")) -> {
                        withStyle(style = SpanStyle(color = Color(0xFFB5CEA8))) { // Xanh lá sáng
                            append(token)
                        }
                    }
                    // Xử lý toán tử và dấu câu
                    token.matches(Regex("""[+\-*/=<>!&|]""")) && token.matches(Regex("""[{}()\[\];,.]""")) -> {
                        append(token)
                    }
                    // Xử lý ký tự đặc biệt (bao gồm # trong hex color)
                    (token.matches(Regex("""[@#$%^]""")) || token.matches(Regex("""\#[0-9A-Fa-f]{3,6}"""))) && language != "python" -> {
                        withStyle(style = SpanStyle(color = Color(0xFFDCDCAA))) { // Vàng nhạt
                            append(token)
                        }
                    }
                    // Xử lý từ khóa
                    keywords.contains(token) -> {
                        withStyle(style = SpanStyle(color = Color(0xFF569CD6), fontWeight = FontWeight.Bold)) {
                            append(token)
                        }
                    }
                    // Xử lý kiểu dữ liệu
                    types.contains(token) -> {
                        withStyle(style = SpanStyle(color = Color(0xFF4EC9B0))) {
                            append(token)
                        }
                    }
                    else -> {
                        append(token)
                    }
                }
                currentIndex = match.range.last + 1
            }

            if (currentIndex < code.length) {
                append(code.substring(currentIndex))
            }
        }
    }
    private fun getKeywordsForLanguage(language: String): Set<String> {
        return when (language) {
            "java" -> setOf(
                "public", "private", "protected", "class", "interface", "if", "else", "for", "while",
                "return", "new", "static", "final", "package", "import", "try", "catch", "finally",
                "synchronized", "throws", "transient", "volatile", "abstract", "extends", "implements"
            )
            "python" -> setOf(
                "def", "class", "if", "else", "elif", "for", "while", "return", "import", "from",
                "as", "with", "try", "except", "True", "False", "None", "and", "or", "not", "is",
                "lambda", "yield", "global", "nonlocal"
            )
            "javascript", "js" -> setOf(
                "function", "var", "let", "const", "if", "else", "for", "while", "return", "class",
                "new", "this", "async", "await", "try", "catch", "finally", "import", "export",
                "default", "super", "extends"
            )
            "csharp", "cs" -> setOf(
                "public", "private", "protected", "class", "interface", "if", "else", "for", "while",
                "return", "new", "static", "namespace", "using", "try", "catch", "finally",
                "abstract", "readonly", "const", "virtual", "override", "sealed", "params"
            )
            "html" -> setOf(
                "div", "span", "p", "a", "img", "html", "head", "body", "script", "style", "table",
                "tr", "td", "th", "form", "input", "meta", "link", "title", "DOCTYPE"
            )
            "ruby" -> setOf(
                "def", "class", "if", "else", "elsif", "for", "while", "return", "module", "end",
                "true", "false", "nil", "require", "include", "extend", "unless", "begin", "rescue"
            )
            "go" -> setOf(
                "package", "import", "func", "var", "const", "type", "interface", "struct", "if",
                "else", "for", "return", "defer", "panic", "recover", "go", "select", "chan"
            )
            "php" -> setOf(
                "class", "public", "private", "protected", "if", "else", "for", "while", "echo",
                "return", "function", "var", "const", "true", "false", "null", "include", "require",
                "try", "catch", "finally"
            )
            "swift" -> setOf(
                "class", "struct", "enum", "func", "var", "let", "if", "else", "for", "while",
                "return", "import", "public", "private", "internal", "nil", "true", "false",
                "defer", "guard", "self"
            )
            "kotlin" -> setOf(
                "fun", "val", "var", "if", "else", "when", "for", "while", "return", "class",
                "object", "interface", "package", "import", "sealed", "data", "enum", "companion",
                "try", "catch", "finally", "this"
            )
            else -> emptySet()
        }
    }

    private fun getTypesForLanguage(language: String): Set<String> {
        return when (language) {
            "java" -> setOf(
                "void", "int", "double", "float", "boolean", "char", "long", "short", "byte",
                "String", "Object", "Integer", "Double", "Float", "Boolean", "Character"
            )
            "python" -> setOf(
                "int", "float", "str", "bool", "list", "dict", "set", "tuple"
            )
            "javascript", "js" -> setOf(
                "Number", "String", "Boolean", "Object", "Array", "Function", "undefined", "null"
            )
            "csharp", "cs" -> setOf(
                "void", "int", "double", "float", "bool", "char", "long", "short", "byte",
                "string", "object"
            )
            "go" -> setOf(
                "int", "int8", "int16", "int32", "int64", "uint", "uint8", "uint16", "uint32",
                "uint64", "float32", "float64", "string", "bool", "byte", "rune"
            )
            "php" -> setOf(
                "int", "float", "string", "bool", "array", "object", "null"
            )
            "swift" -> setOf(
                "Int", "Double", "Float", "Bool", "String", "Character", "Array", "Dictionary",
                "Set"
            )
            "kotlin" -> setOf(
                "Int", "Double", "Float", "Boolean", "String", "Char", "Long", "Short", "Byte",
                "Unit"
            )
            else -> emptySet()
        }
    }
}
