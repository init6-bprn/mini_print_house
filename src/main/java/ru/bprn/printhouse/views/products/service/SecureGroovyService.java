package ru.bprn.printhouse.views.products.service;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.SecureASTCustomizer;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Сервис для безопасного выполнения Groovy-скриптов в "песочнице".
 */
@Service
public class SecureGroovyService {

    private final CompilerConfiguration sandboxedConfig;
    private final GroovyShell sandboxedShell;

    public SecureGroovyService() {
        // 1. Создаем конфигурацию компилятора
        CompilerConfiguration config = new CompilerConfiguration();

        // 2. Создаем и настраиваем "песочницу"
        final SecureASTCustomizer customizer = new SecureASTCustomizer();

        // Белый список разрешенных для импорта классов
        customizer.setAllowedImports(List.of(
                "java.lang.Math"
        ));

        // Черный список токенов (ключевых слов)
        customizer.setDisallowedTokens(List.of(
                org.codehaus.groovy.syntax.Types.KEYWORD_WHILE,
                org.codehaus.groovy.syntax.Types.KEYWORD_GOTO
        ));

        // Запрещаем вызов методов у определенных классов.
        // Это предотвращает выполнение команд ОС через "some string".execute()
        customizer.setDisallowedReceivers(List.of(
                java.lang.String.class.getName()
        ));

        // Добавляем наш кастомайзер в конфигурацию
        config.addCompilationCustomizers(customizer);

        // 3. Создаем GroovyShell с этой безопасной конфигурацией
        this.sandboxedConfig = config;
        this.sandboxedShell = new GroovyShell(this.sandboxedConfig);
    }

    /**
     * Выполняет скрипт в безопасной среде.
     * @param context Контекст с переменными, доступными в скрипте.
     * @param formula Строка с Groovy-кодом.
     * @return Результат выполнения скрипта.
     */
    public Object evaluate(Map<String, Object> context, String formula) {
        Binding binding = new Binding(context);
        Script script = sandboxedShell.parse(formula);
        script.setBinding(binding);
        return script.run();
    }

    /**
     * Возвращает безопасную конфигурацию компилятора для использования в других сервисах (например, для валидации).
     */
    public CompilerConfiguration getSandboxedConfig() {
        return sandboxedConfig;
    }
}