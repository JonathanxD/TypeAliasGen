/**
 *      TypeAliasGen - Typealias generator
 *
 *         The MIT License (MIT)
 *
 *      Copyright (c) 2016 JonathanxD <https://github.com/JonathanxD/TypeAliasGen>
 *      Copyright (c) contributors
 *
 *
 *      Permission is hereby granted, free of charge, to any person obtaining a copy
 *      of this software and associated documentation files (the "Software"), to deal
 *      in the Software without restriction, including without limitation the rights
 *      to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *      copies of the Software, and to permit persons to whom the Software is
 *      furnished to do so, subject to the following conditions:
 *
 *      The above copyright notice and this permission notice shall be included in
 *      all copies or substantial portions of the Software.
 *
 *      THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *      IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *      FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *      AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *      LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *      OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *      THE SOFTWARE.
 */
package com.github.jonathanxd.typealiasgen

import com.github.jonathanxd.typealiasgen.util.duplicates

object Gen {

    fun gen(elements: List<Element>,
            packageName: String? = null,
            basePackageName: String? = null,
            prefix: String = "",
            suffix: String = "",
            nameResolver: (Element) -> String = Element::simpleName,
            receiver: (String) -> Unit,
            finisher: () -> Unit) {

        val mapped = mapToName(elements, basePackageName, nameResolver)

        packageName?.let {
            if (it.isNotEmpty()) {
                receiver("package $packageName")
                receiver("")
            }
        }

        mapped.forEach { k, v ->
            val typeAliasName = "$prefix$k$suffix"

            receiver("typealias $typeAliasName = $v")
        }

        finisher()
    }

    fun mapToName(elements: List<Element>, basePackageName: String?, nameResolver: (Element) -> String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val duplicatedNames = elements.map { nameResolver(it) }.duplicates()

        val basePackage = if (basePackageName != null) "$basePackageName." else ""

        elements.forEach {
            val resolvedName = nameResolver(it).let { name ->
                if (duplicatedNames.contains(name))
                    this.uniqueName(duplicatedNames, name, it)
                else
                    name
            }

            val aliasPath = "$basePackage${it.genName}"

            map += fixReservedName(resolvedName) to fixReservedName(aliasPath)
        }

        return map
    }

    private fun fixReservedName(name: String): String = if (name.contains('.')) {
        name.split(".").map(this::fixReservedName_).joinToString(separator = ".")
    } else {
        this.fixReservedName_(name)
    }


    private fun fixReservedName_(name: String): String = when (name) {
        "package", "as", "typealias", "class", "this", "super", "val", "var",
        "fun", "for", "null", "true", "false", "is", "in", "throw", "return",
        "break", "continue", "object", "if", "try", "else", "while", "do",
        "when", "interface", "typeof", "yield", "sealed", "async" -> "`$name`"
        else -> name
    }


    private fun uniqueName(set: Set<String>, name: String, element: Element): String {
        val packageParts = if (element.packageName.isEmpty()) emptyList() else element.packageName.split(".").map(String::capitalize)
        var endName = name
        var i = packageParts.size - 1

        while (set.contains(endName) && i > -1) {
            endName = "${packageParts[i]}$endName"
            --i
        }

        if (set.contains(endName))
            throw IllegalStateException("Cannot generate a unique name for element '$element' with name: '$name', generatedName: '$endName'")

        return endName
    }
}