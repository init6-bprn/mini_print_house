package ru.bprn.printhouse.views.templates.service;

import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;
import org.springframework.stereotype.Service;
import ru.bprn.printhouse.views.templates.entity.Variable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Service
public class FormulaValidationService {

    // Список глобально доступных переменных/классов.
    // В будущем можно вынести в application.properties или в базу данных.
    private static final Set<String> PREDEFINED_VARIABLES = Set.of(
            "Math", "BigDecimal", "RoundingMode"
    );

    public ValidationResult validate(String formula, List<Variable>... availableVariableLists) {
        if (formula == null || formula.isBlank()) {
            return ValidationResult.success(); // Пустая формула считается валидной
        }

        // 1. Собираем все доступные переменные в один Set
        Set<String> availableVariableNames = new HashSet<>(PREDEFINED_VARIABLES);
        if (availableVariableLists != null) {
            Stream.of(availableVariableLists)
                    .map(list -> list == null ? Collections.<Variable>emptyList() : list) // Защита от null-списков
                    .flatMap(Collection::stream)
                    .map(Variable::getKey)
                    .forEach(availableVariableNames::add);
        }

        // 2. Проверяем синтаксис и собираем используемые переменные
        Set<String> usedVariableNames = new HashSet<>();
        try {
            CompilerConfiguration config = new CompilerConfiguration();
            CompilationUnit unit = new CompilationUnit(config);
            SourceUnit su = unit.addSource("formula.groovy", formula);

            // Компилируем до фазы, когда построено AST (Abstract Syntax Tree)
            unit.compile(Phases.CONVERSION);

            // Обходим AST для сбора всех использованных переменных
            su.getAST().getStatementBlock().visit(new CodeVisitorSupport() {
                @Override
                public void visitVariableExpression(VariableExpression expression) {
                    // 'it' - это специальная переменная в Groovy, ее не нужно проверять
                    if (!"it".equals(expression.getName())) {
                        usedVariableNames.add(expression.getName());
                    }
                    super.visitVariableExpression(expression);
                }
            });

        } catch (CompilationFailedException e) {
            // Ошибка синтаксиса
            String errorMessage = e.getMessage().lines().findFirst().orElse("Синтаксическая ошибка в формуле");
            return ValidationResult.failure("Синтаксическая ошибка: " + errorMessage);
        } catch (Exception e) {
            return ValidationResult.failure("Неожиданная ошибка при анализе формулы: " + e.getMessage());
        }

        // 3. Сравниваем используемые переменные с доступными
        Set<String> undefinedVariables = new HashSet<>(usedVariableNames);
        undefinedVariables.removeAll(availableVariableNames);

        if (!undefinedVariables.isEmpty()) {
            return ValidationResult.failure("Найдены несуществующие переменные: " + String.join(", ", undefinedVariables));
        }

        return ValidationResult.success();
    }

    // Вспомогательный класс для результата валидации
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult failure(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
