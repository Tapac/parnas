package sndl.parnas.storage.impl

import sndl.parnas.storage.Storage
import sndl.parnas.storage.ConfigOption
import sndl.parnas.utils.*
import java.io.File
import java.util.*

class Plain(name: String, private val path: String) : Storage(name) {
    constructor(name: String, config: Map<String, String>) :
            this(name = name, path = getConfigParameter("path", config))

    private val file = File(path)

    private lateinit var data: Properties

    override val isInitialized: Boolean = file.exists()

    init {
        if (file.exists()) {
            loadData()
        }
    }

    private fun loadData() {
        data = Properties().apply {
            file.reader().use { reader -> load(reader) }
        }
    }

    private fun storeData() {
        file.outputStream().use {
            stream -> data.store(stream, null)
        }
    }

    override fun initialize() {
        if (isInitialized) {
            throw CannotInitializeStorage("Storage is already initialized")
        }

        file.createNewFile()
        loadData()
    }

    override fun list() = data.map { ConfigOption(it.key.toString(), it.value.toString()) }.toLinkedSet()

    override fun get(key: String): ConfigOption? = data.getProperty(key)?.let { ConfigOption(key, it) }

    override fun set(key: String, value: String): ConfigOption {
        data.setProperty(key, value)
        storeData()

        return ConfigOption(key, value)
    }

    override fun delete(key: String) {
        data.remove(key)
        storeData()
    }
}
