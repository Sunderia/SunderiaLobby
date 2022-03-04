package fr.sunderia.lobby.commands.info;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandInfo {

    String name();
    String permission() default "";
    boolean requiresPlayer();
    String[] aliases() default {};
    String usage() default "";
    String description() default "";
    String permissionMessage() default "§cYou don't have permission to use this command.";
}
