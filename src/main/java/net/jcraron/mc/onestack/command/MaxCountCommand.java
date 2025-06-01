package net.jcraron.mc.onestack.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.jcraron.mc.onestack.api.OneStackConfig;
import net.jcraron.mc.onestack.config.value.CountNumberValue;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceOrTagArgument;
import net.minecraft.commands.arguments.ResourceOrTagArgument.Result;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

public class MaxCountCommand {

	public static LiteralArgumentBuilder<CommandSourceStack> createCommand(CommandBuildContext context) {
		return Commands.literal("maxcount").requires((p_137777_) -> p_137777_.hasPermission(2))
				.then(createSetCommand(context))
				.then(createRemoveCommand(context))
				.then(createReloadCommand(context));
	}

	private static ArgumentBuilder<CommandSourceStack, ?> createReloadCommand(CommandBuildContext context) {
		return Commands.literal("reload").executes(MaxCountCommand::reloadConfig);
	}

	private static ArgumentBuilder<CommandSourceStack, ?> createSetCommand(CommandBuildContext context) {
		return Commands.literal("set")
				.then(Commands.argument("item", ResourceOrTagArgument.resourceOrTag(context, Registries.ITEM))
						.then(Commands.literal("max")
								.executes((c) -> setMaxCount(c, CountNumberValue.JAVA_VALUE_MAX, null))
								.then(Commands.argument("priority", IntegerArgumentType.integer())
										.executes((c) -> setMaxCount(c, CountNumberValue.JAVA_VALUE_MAX, getPriority(c)))))
						.then(Commands.literal("default")
								.executes((c) -> setMaxCount(c, CountNumberValue.JAVA_VALUE_DEFAULT, null))
								.then(Commands.argument("priority", IntegerArgumentType.integer())
										.executes((c) -> setMaxCount(c, CountNumberValue.JAVA_VALUE_DEFAULT, getPriority(c)))))
						.then(Commands.argument("count", IntegerArgumentType.integer(1, CountNumberValue.JAVA_VALUE_MAX))
								.executes((c) -> setMaxCount(c, getCount(c), null))
								.then(Commands.argument("priority", IntegerArgumentType.integer(1, CountNumberValue.JAVA_VALUE_MAX))
										.executes((c) -> setMaxCount(c, getCount(c), getPriority(c))))));
	}

	private static ArgumentBuilder<CommandSourceStack, ?> createRemoveCommand(CommandBuildContext context) {
		return Commands.literal("unset")
				.then(Commands.argument("item", ResourceOrTagArgument.resourceOrTag(context, Registries.ITEM))
						.executes((c) -> unsetMaxCount(c)));
	}

	private static int unsetMaxCount(CommandContext<CommandSourceStack> command) throws CommandSyntaxException {
		Result<Item> resource = ResourceOrTagArgument.getResourceOrTag(command, "item", Registries.ITEM);
		resource.unwrap().left().ifPresent((item) -> OneStackConfig.unset(item.get()));
		resource.unwrap().right().ifPresent((tag) -> OneStackConfig.unset(tag.key()));
		String message = String.format("unset max count of %s", resource.asPrintable());
		command.getSource().sendSystemMessage(Component.literal(message));
		return Command.SINGLE_SUCCESS;
	}

	private static int setMaxCount(CommandContext<CommandSourceStack> command, int count, Integer priority)
			throws CommandSyntaxException {
		Result<Item> resource = ResourceOrTagArgument.getResourceOrTag(command, "item", Registries.ITEM);
		resource.unwrap().left().ifPresent((item) -> OneStackConfig.set(item.get(), count,
				priority != null ? priority : OneStackConfig.PRIORITY_DEFAULT_ITEM));
		resource.unwrap().right().ifPresent((tag) -> OneStackConfig.set(tag.key(), count,
				priority != null ? priority : OneStackConfig.PRIORITY_DEFAULT_TAG));
		String message = String.format("set max count of %s to %s ", resource.asPrintable(),
				CountNumberValue.INSTANCE.toJsonValue(count));
		command.getSource().sendSystemMessage(Component.literal(message));
		return Command.SINGLE_SUCCESS;
	}

	private static int reloadConfig(CommandContext<CommandSourceStack> command)
			throws CommandSyntaxException {
		String message = String.format("reload \"One Stack\" mod config");
		command.getSource().sendSystemMessage(Component.literal(message));
		OneStackConfig.reload();
		return Command.SINGLE_SUCCESS;
	}

	private static int getPriority(CommandContext<CommandSourceStack> command) {
		return IntegerArgumentType.getInteger(command, "priority");
	}

	private static int getCount(CommandContext<CommandSourceStack> command) {
		return IntegerArgumentType.getInteger(command, "count");
	}
}