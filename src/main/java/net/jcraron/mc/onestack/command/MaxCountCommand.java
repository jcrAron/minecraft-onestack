package net.jcraron.mc.onestack.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.jcraron.mc.onestack.OneStackMod;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = OneStackMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MaxCountCommand {

	// maxcount <item> default
	// maxcount <item> max
	// maxcount <item> <count>
	private static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
		dispatcher.register(Commands.literal("maxcount").requires((p_137777_) -> {
			return p_137777_.hasPermission(2);
		}).then(Commands.argument("item", ItemArgument.item(context))
				.then(Commands.literal("max").executes((command) -> {
					return setToMax(command);
				})).then(Commands.literal("default").executes((command) -> {
					return setToDefault(command);
				})).then(Commands.argument("count", IntegerArgumentType.integer(1)).executes((command) -> {
					return setToValue(command);
				}))));
	}

	private static int setToValue(CommandContext<CommandSourceStack> command) {
		System.out.println("hello!!!");
		ItemInput itemInput = ItemArgument.getItem(command, "item");
		int count = IntegerArgumentType.getInteger(command, "count");
		command.getSource().sendSystemMessage(Component
				.literal("set max count of " + ItemArgument.getItem(command, "item").serialize() + " to " + count));
		OneStackMod.setMaxCount(itemInput.getItem(), count);
		return Command.SINGLE_SUCCESS;
	}

	private static int setToMax(CommandContext<CommandSourceStack> command) {
		ItemInput itemInput = ItemArgument.getItem(command, "item");
		command.getSource().sendSystemMessage(Component.literal(
				"set max count of " + ItemArgument.getItem(command, "item").serialize() + " to " + Integer.MAX_VALUE));
		OneStackMod.setMaxCount(itemInput.getItem(), Integer.MAX_VALUE);
		return Command.SINGLE_SUCCESS;
	}

	private static int setToDefault(CommandContext<CommandSourceStack> command) {
		ItemInput itemInput = ItemArgument.getItem(command, "item");
		command.getSource().sendSystemMessage(Component
				.literal("set max count of " + ItemArgument.getItem(command, "item").serialize() + " to default"));
		OneStackMod.setMaxCountToDefault(itemInput.getItem());
		return Command.SINGLE_SUCCESS;
	}

	@SubscribeEvent
	public static void registerCommands(RegisterCommandsEvent event) {
		MaxCountCommand.register(event.getDispatcher(), event.getBuildContext());
	}
}
