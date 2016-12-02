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

import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import java.io.File
import java.lang.reflect.Modifier
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import kotlin.streams.toList

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        System.err.println("Invalid input arguments. Usage:")
        System.err.flush()
        showHelp()
        System.exit(9)
    }

    if (args[0] == "--help")
        showHelp()

    val dir = args[0]

    val packageName = args.getOrElse(1, { null })?.orNull()
    val basePackageName = args.getOrElse(2, { null })?.orNull()
    val prefix = args.getOrElse(3, { "" }).orDefault()
    val suffix = args.getOrElse(4, { "" }).orDefault()
    val output = args.getOrElse(5, { null })?.orNull()
    val analyzeCp = args.getOrElse(6, { null })?.orNull() != null

    val elements =
            if (!analyzeCp)
                TypeAliasGen.fromFiles(dir)
            else
                TypeAliasGen.fromCp(dir)

    val sb = StringBuilder()

    val receiver: (String) -> Unit =
            if (output == null)
                ::println
            else {
                {
                    sb.append("$it\n")
                }
            }

    val finisher: () -> Unit =
            if (output == null) {
                {}
            } else {
                {
                    val file = File(output)
                    file.parentFile?.let(File::mkdirs)
                    if (file.exists())
                        file.delete()

                    Files.write(file.toPath(), sb.toString().toByteArray(charset = Charsets.UTF_8), StandardOpenOption.CREATE)
                }
            }


    Gen.gen(elements = elements,
            packageName = packageName,
            basePackageName = basePackageName,
            prefix = prefix,
            suffix = suffix,
            receiver = receiver,
            finisher = finisher)
}

private fun String.orDefault() = if (this == "~") "" else this
private fun String.orNull() = if (this == "~") null else this

private fun showHelp() {
    val name = try {
        File(TypeAliasGen::class.java.protectionDomain.codeSource.location.toURI()).absolutePath
    } catch (e: Exception) {
        "[TypeAliasGen jar]"
    }

    println("[] = required, <> = optional")
    println("UTF-8 Character ~ = default value")

    println("java -jar $name [dir] <package> <basePackage> <prefix> <suffix> <output> <analyzeCp>")
}

object TypeAliasGen {
    fun fromFiles(baseDir: String): List<Element> {
        val dir = Paths.get(baseDir)
        return Files.walk(dir).use {
            it.filter {
                Files.isRegularFile(it) &&
                        (it.fileName.toString().endsWith(".java")
                                || it.fileName.toString().endsWith(".kt")
                                || (it.fileName.toString().endsWith(".class") && !it.fileName.toString().contains("$")))
            }.map {
                val name = it.toQualifiedName(dir.nameCount)
                Element(name, name)
            }.toList()
        }
    }

    fun fromClasses(classes: Iterable<Class<*>>) = classes
            .filter { it.canonicalName != null && Modifier.isPublic(it.modifiers) }
            .map { Element(getGenName(it), it.canonicalName) }
            .distinct()

    fun fromCp(basePackage: String) =
            fromClasses(
                    Reflections(basePackage, SubTypesScanner(false))
                            .getSubTypesOf(Object::class.java)
            )

    fun getGenName(klass: Class<*>): String {
        val name = klass.canonicalName
        val typeParameters = klass.typeParameters

        if(typeParameters.isEmpty())
            return name

        return "$name${typeParameters.map { "*" }.joinToString(separator = ",", prefix = "<", postfix = ">")}"
    }
}

fun Path.toQualifiedName(nameOffset: Int = 0) =
        (nameOffset..this.nameCount - 1)
                .map { this.getName(it).toString() }
                .joinToString(separator = ".")
                .let {
                    if (it.endsWith(".class"))
                        it.substring(0..(it.lastIndexOf(".class") - 1))
                    if (it.endsWith(".kt"))
                        it.substring(0..(it.lastIndexOf(".kt") - 1))
                    else if (it.endsWith(".java"))
                        it.substring(0..(it.lastIndexOf(".java") - 1))
                    else it
                }