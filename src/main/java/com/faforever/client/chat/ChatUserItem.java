package com.faforever.client.chat;

import javafx.scene.control.TreeItem;
import lombok.ToString;
import lombok.Value;

@Value
@ToString(of = {"chatChannelUser"})
class ChatUserItem {
  ChatChannelUser chatChannelUser;
  TreeItem<Object> treeItem;
}
