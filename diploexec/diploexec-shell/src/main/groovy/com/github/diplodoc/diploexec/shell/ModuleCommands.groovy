package com.github.diplodoc.diploexec.shell

import com.github.diplodoc.diplobase.client.ModuleClient
import com.github.diplodoc.diplobase.domain.diploexec.Module
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ResourceLoader
import org.springframework.shell.core.CommandMarker
import org.springframework.shell.core.annotation.CliCommand
import org.springframework.shell.core.annotation.CliOption
import org.springframework.stereotype.Component

/**
 * @author yaroslav.yermilov
 */
@Component
class ModuleCommands implements CommandMarker {

    @Autowired
    ResourceLoader resourceLoader

    ModuleClient moduleClient = new ModuleClient('http://localhost:8080')

    @CliCommand(value = 'module list', help = 'list all modules')
    String list() {
        moduleClient.modules()
                        .collect { Module module ->
                            "${module.id}".padLeft(5) + "${module.name}".padLeft(30)
                        }
                        .join('\n')
    }

    @CliCommand(value = 'module get', help = 'get full description of module')
    String get(@CliOption(key = '', mandatory = true, help = 'module name') final String name) {
        Module module = moduleClient.findOneByName(name)

        "id:".padRight(20) + "${module.id}\n" +
        "name:".padRight(20) + "${module.name}\n" +
        "definition:\n" + "${module.definition}"
    }

    @CliCommand(value = 'module update', help = 'update module description')
    String update(@CliOption(key = 'name', mandatory = true, help = 'module name') final String name,
                  @CliOption(key = 'definition', mandatory = true, help = 'path to definition file') final String pathToDefinitionFile) {
        Module module = moduleClient.findOneByName(name)
        module.definition = resourceLoader.getResource("file:${pathToDefinitionFile}").file.text
        module = moduleClient.save(module)

        "id:".padRight(20) + "${module.id}\n" +
        "name:".padRight(20) + "${module.name}\n" +
        "definition:\n" + "${module.definition}"
    }
}
