package com.faforever.client.chat;

import com.faforever.client.theme.UiService;
import javafx.scene.control.TreeTableCell;

import java.util.Optional;

public class ChatUserTreeTableCell extends TreeTableCell<ChatUserItem, Object> {

  private final ChatUserItemController chatUserItemController;
  private final ChatUserItemCategoryController chatUserCategoryController;
  private Optional<Runnable> onSocialStatusUpdatedListener;
  private Object oldItem;

  public ChatUserTreeTableCell(UiService uiService) {
    chatUserItemController = uiService.loadFxml("theme/chat/chat_user_item.fxml");
    chatUserCategoryController = uiService.loadFxml("theme/chat/chat_user_category.fxml");
  }

  public void setOnSocialStatusUpdatedListener(Runnable onSocialStatusUpdatedListener) {
    this.onSocialStatusUpdatedListener = Optional.ofNullable(onSocialStatusUpdatedListener);
  }

  @Override
  protected void updateItem(Object item, boolean empty) {
    if (item == oldItem) {
      return;
    }
    oldItem = item;

    super.updateItem(item, empty);

    setText(null);
    if (item == null || empty) {
      setGraphic(null);
      return;
    }

    if (item instanceof ChatUserItem && ((ChatUserItem) item).getChatChannelUser() != null) {
      chatUserItemController.setChatUser(((ChatUserItem) item).getChatChannelUser());
      chatUserItemController.setOnSocialStatusUpdatedListener(onSocialStatusUpdatedListener);
      setText(null);
      setGraphic(chatUserItemController.getRoot());
    } else if (item instanceof ChatUserCategory) {
      chatUserCategoryController.setChatUserCategory((ChatUserCategory) item);
      setText(null);
      setGraphic(chatUserCategoryController.getRoot());
    } else {
      setText(item.toString());
      setGraphic(null);
    }
  }
}
