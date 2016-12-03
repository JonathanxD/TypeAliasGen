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

object Gen {

    fun gen(elements: List<Element>,
            packageName: String? = null,
            basePackageName: String? = null,
            prefix: String = "",
            suffix: String = "",
            nameResolver: (Element) -> String = Element::simpleName,
            receiver: (String) -> Unit,
            finisher: () -> Unit) {

        val names = mutableMapOf<String, String>()

        packageName?.let {
            if(it.isNotEmpty()) {
                receiver("package $packageName")
                receiver("")
            }
        }

        val basePackage = if(basePackageName != null) "$basePackageName." else ""

        elements.forEach {
            val resolvedName: String = nameResolver(it).let{ name ->
                if(names.containsKey(name))
                    this.uniqueName(names, name, it)
                else
                    name
            }

            val aliasPath = "$basePackage${it.genName}"
            val typeAliasName = "$prefix$resolvedName$suffix"

            names += resolvedName to aliasPath

            receiver("typealias $typeAliasName = $aliasPath")
        }

        finisher()
    }

    private fun uniqueName(map: Map<String, String>, name: String, element: Element): String {
        val packageParts = if(element.packageName.isEmpty()) emptyList() else element.packageName.split(".").map(String::capitalize)
        var endName = name
        var i = packageParts.size - 1

        while(map.containsKey(endName) && i > -1) {
            endName = "${packageParts[i]}$endName"
            --i
        }

        if(map.containsKey(endName))
            throw IllegalStateException("Cannot generate a unique name for element '$element' with name: '$name', generatedName: '$endName'")

        return endName
    }
}