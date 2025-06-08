// File: org/example/ReplyListCell.java
package org.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.Random;
import java.util.function.BiConsumer;

class ReplyListCell extends ListCell<Reply> {
    private VBox card;
    private Text authorTextNode;
    private Text contentTextNode;
    private Text dateTextNode;
    private Button likeButton;
    private Button dislikeButton;
    private Button replyButtonSmall;
    private StackPane avatarPane;
    private Circle avatarCircle;
    private Text avatarLetter;
    private Text karmaTextNode;

    private final BiConsumer<Review, Reply> replyActionHandler;
    private final QuadConsumer<Integer, String, Reply, ListView<Reply>> likeDislikeHandler;
    private final Random random = new Random();
    private final Color[] avatarColors = {
            Color.valueOf("#4A90E2"), Color.valueOf("#50E3C2"), Color.valueOf("#F5A623"),
            Color.valueOf("#BD10E0"), Color.valueOf("#9013FE"), Color.valueOf("#D0021B"),
            Color.valueOf("#F8E71C"), Color.valueOf("#7ED321"), Color.valueOf("#417505")
    };

    @FunctionalInterface
    public static interface QuadConsumer<T, U, V, W> {
        void accept(T t, U u, V v, W w);
    }

    public ReplyListCell(BiConsumer<Review, Reply> replyActionHandler, QuadConsumer<Integer, String, Reply, ListView<Reply>> likeDislikeHandler) {
        this.replyActionHandler = replyActionHandler;
        this.likeDislikeHandler = likeDislikeHandler;

        avatarCircle = new Circle(14);
        avatarLetter = new Text();
        avatarLetter.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        avatarLetter.setFill(Color.WHITE);
        avatarPane = new StackPane(avatarCircle, avatarLetter);
        avatarPane.setAlignment(Pos.CENTER);

        authorTextNode = new Text();
        authorTextNode.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        authorTextNode.setFill(Color.valueOf("#E0E0E0"));

        contentTextNode = new Text();
        contentTextNode.setFont(Font.font("Arial", 14));
        contentTextNode.setFill(Color.valueOf("#C8C8C8"));
        contentTextNode.setWrappingWidth(480);

        dateTextNode = new Text();
        dateTextNode.setFont(Font.font("Arial", 14));
        dateTextNode.setFill(Color.valueOf("#888888"));

        karmaTextNode = new Text();
        karmaTextNode.setFont(Font.font("Arial", FontWeight.NORMAL, 14));

        HBox authorLine = new HBox(8, avatarPane, authorTextNode, new Region(), dateTextNode);
        HBox.setHgrow(authorLine.getChildren().get(2), Priority.ALWAYS);
        authorLine.setAlignment(Pos.CENTER_LEFT);

        likeButton = ButtonStyle.createStyledButton("👍 (0)");
        dislikeButton = ButtonStyle.createStyledButton("👎 (0)");
        replyButtonSmall = ButtonStyle.expandPaneStyledButton("↪ Reply");

        HBox actions = new HBox(10, likeButton, dislikeButton, replyButtonSmall);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox contentAndKarmaBox = new VBox(5, karmaTextNode, contentTextNode);

        card = new VBox(8, authorLine, contentAndKarmaBox, actions);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: #101010; -fx-border-color: #2A2A2A; -fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6;");
        setStyle("-fx-background-color: black; -fx-padding: 5 0 5 0;");
    }

    private void updateAvatar(String authorName) {
        if (authorName != null && !authorName.trim().isEmpty()) {
            avatarLetter.setText(authorName.trim().substring(0, 1).toUpperCase());
            avatarCircle.setFill(avatarColors[Math.abs(authorName.hashCode()) % avatarColors.length]);
        } else {
            avatarLetter.setText("?");
            avatarCircle.setFill(Color.valueOf("#333333"));
        }
    }

    @Override
    protected void updateItem(Reply reply, boolean empty) {
        super.updateItem(reply, empty);
        if (empty || reply == null) {
            setGraphic(null);
        } else {
            updateAvatar(reply.getAuthorName());
            authorTextNode.setText(reply.getAuthorName() + " (" + reply.getCreatorStatus() + ")");
            contentTextNode.setText(reply.getReplyContent());
            dateTextNode.setText(reply.getFormattedCreationDate());
            likeButton.setText("👍 (" + reply.getLikesCount() + ")");
            dislikeButton.setText("👎 (" + reply.getDislikesCount() + ")");
            int karma = reply.getKarma();
            karmaTextNode.setText("Karma: " + karma);
            karmaTextNode.setFill(karma >= 0 ? Color.valueOf("#6DD400") : Color.valueOf("#E53935"));

            Review dummyParentReview = new Review(reply.getParentReviewId(),0,"","","",0,null,null);

            replyButtonSmall.setOnAction(e -> {
                if (replyActionHandler != null) {
                    replyActionHandler.accept(dummyParentReview, reply);
                }
            });

            likeButton.setOnAction(e -> {
                if(likeDislikeHandler != null) {
                    likeDislikeHandler.accept(reply.getReplyId(), "like", reply, getListView());
                }
            });
            dislikeButton.setOnAction(e -> {
                if(likeDislikeHandler != null) {
                    likeDislikeHandler.accept(reply.getReplyId(), "dislike", reply, getListView());
                }
            });
            setGraphic(card);
        }
    }
}