package ru.a1024bits.bytheway.router

import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.Router


/**
 * Created by andrey.gusenkov on 12/10/2017.
 */
class LocalCiceroneHolder {
    private val containers: HashMap<String, Cicerone<Router>>
    
    init {
        containers = HashMap()
    }
    
    fun getCicerone(containerTag: String): Cicerone<Router>? {
        if (!containers.containsKey(containerTag)) {
            containers.put(containerTag, Cicerone.create())
        }
        return containers[containerTag]
    }
}