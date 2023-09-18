package dev.felnull.itts.core.discord.command;

import dev.felnull.itts.core.ITTSRuntimeUse;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

/**
 * 全コマンドのベース
 *
 * @author MORIMORI0317
 */
public abstract class BaseCommand implements ITTSRuntimeUse {
    /**
     * メンバーのデフォルト権限
     */
    protected static final DefaultMemberPermissions MEMBERS_PERMISSIONS = DefaultMemberPermissions.enabledFor(Permission.VOICE_CONNECT, Permission.MESSAGE_SEND);

    /**
     * オーナーのデフォルト権限
     */
    protected static final DefaultMemberPermissions OWNERS_PERMISSIONS = DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER);

    /**
     * コマンド名
     */
    @NotNull
    protected final String name;

    /**
     * コンストラクタ
     *
     * @param name コマンド名
     */
    protected BaseCommand(@NotNull String name) {
        this.name = name;
    }

    /**
     * このコマンドを実行しようとしているスラッシュコマンドイベントかどうか確認
     *
     * @param event スラッシュコマンドイベント
     * @return このコマンドを実行しようとしているかどうか
     */
    public boolean isCommandMatch(SlashCommandInteractionEvent event) {
        return name.equals(event.getName()) && event.getGuild() != null && event.getMember() != null;
    }

    /**
     * このコマンドの自動補完イベントかどうか確認
     *
     * @param event 自動補完イベント
     * @return このコマンドの自動補完イベントかどうか
     */
    public boolean isAutoCompleteMatch(CommandAutoCompleteInteractionEvent event) {
        return name.equals(event.getName()) && event.getGuild() != null && event.getMember() != null;
    }

    /**
     * このコマンドのスラッシュコマンドを作成
     *
     * @return スラッシュコマンド
     */
    @NotNull
    public abstract SlashCommandData createSlashCommand();

    /**
     * スラッシュコマンドイベントからこのコマンドを実行
     *
     * @param event スラッシュコマンドイベント
     */
    public abstract void commandInteraction(SlashCommandInteractionEvent event);

    /**
     * 自動補完イベントからコマンドを実行
     *
     * @param event 自動補完イベント
     */
    public void autoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
    }
}
