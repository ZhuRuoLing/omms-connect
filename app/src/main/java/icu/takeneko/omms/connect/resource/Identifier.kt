package icu.takeneko.omms.connect.resource

class Identifier(val namespace: String, val value: String) {
    constructor(string: String) : this(
        string.split(":")[0],
        string.split(":").run { subList(1, this.size - 1) }.joinToString(separator = ":"))
}