package riskfx.app.view;

import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;

import appfx.ui.UiContext;
import appfx.util.FxmlView;
import freetimelabs.io.reactorfx.flux.FxFlux;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import riskfx.engine.GameConfig;
import riskfx.engine.PlayerAssociation;
import riskfx.engine.games.BigEuropeGameConfig;
import riskfx.mapeditor.Bind;

public class NewGame extends FxmlView {

	public static enum Mode {
		LOCAL() {

			@Override
			void configure(NewGame screen) {
				// TODO Auto-generated method stub
				
			}
			
		};
		
		/* package */ abstract void configure(final NewGame screen);
	}
	
	private final ObjectProperty<Mode> modeProperty = new SimpleObjectProperty<>(this, "mode") {
		@Override protected void invalidated() {
			final Mode mode = Optional.ofNullable(get()).orElse(Mode.LOCAL);
			mode.configure(NewGame.this);
		}
	};
	public final ObjectProperty<Mode> modeProperty()	{	return this.modeProperty; }
	public final Mode getMode()							{	return this.modeProperty.getValue(); }
	public final void setMode(final Mode mode)			{	this.modeProperty.setValue(mode); }
	
	private final ObjectProperty<GameConfig> configProperty = new SimpleObjectProperty<>(this, "config") {
		@Override protected void invalidated() {
			onGameConfigChanged(get());
		}
	};
	public final ObjectProperty<GameConfig> configProperty()	{	return this.configProperty; }
	
	public final GameConfig getGameConfig()					{	return this.configProperty.get(); }
	public final void setGameConfig(final GameConfig cfg) {	this.configProperty.setValue(cfg); }
	
	private final ListProperty<PlayerAssociation> playerAssociations = new SimpleListProperty<>(this, "playerAssociations", FXCollections.observableArrayList());
	
	private final UiContext context;

	@Inject
	public NewGame(final UiContext context) {
		this.context = Objects.requireNonNull(context);

		inflateView();

		setMode(Mode.LOCAL);
		setGameConfig(new BigEuropeGameConfig());
	}
	
	protected void onGameConfigChanged(GameConfig gameConfig) {
		this.playerAssociations.set(gameConfig.playerAssociations());
		
		startBtn.disableProperty().unbind();
		startBtn.disableProperty().bind(gameConfig.readyProperty().not());
	}

	@FXML public final void initialize() {
		playersList.setCellFactory(PlayerAssociationCell.factory(Stream.of(PlayerAssociation.StandardTypes.HUMAN, PlayerAssociation.StandardTypes.NONE)));
		playersList.setItems(this.playerAssociations);
		
		FxFlux.from(configProperty)
			.switchMap(gc -> FxFlux.from(gc.cardStyleProperty()))
//			.defaultIfEmpty(GameConfig.CardStyle.FIXED)
			.subscribe(cs -> {
				switch (cs) {
				case FIXED: 
					this.cardStyleToggleGroup.selectToggle(cardsFixedBtn);
					break;
				case INCREASING: 
					this.cardStyleToggleGroup.selectToggle(cardsIncreasingBtn);
					break;
				case ITALIAN:
					this.cardStyleToggleGroup.selectToggle(cardsItalianBtn);
					break;
				}
			});
		
		FxFlux.from(configProperty)
			.switchMap(gc -> FxFlux.from(gc.gameTypeProperty()))
			.map(this::findButtonForGameType)
			.subscribe(rb -> {
				this.gameTypeToggleGroup.selectToggle(rb);
			});
		
		FxFlux.from(configProperty)
			.map(GameConfig::getPreviewImageUrl)
			.map(URL::toExternalForm)
			.map(Image::new)
			.subscribe(img -> {
				previewImage.setImage(img);
			});
		
		Bind.bind(autoAssignBtn.selectedProperty(), configProperty, GameConfig::autoAssignProperty);
		Bind.bind(autoPlaceBtn.selectedProperty(), configProperty, GameConfig::autoPlaceProperty);
	
		
	}
	
	private RadioButton findButtonForGameType(final GameConfig.Type type) {
		switch (type) {
		case DOMINATION:
			return dominationBtn;
		case MISSION:
			return missionBtn;
		case CAPITAL:
			return capitalBtn;
			default:
				throw new IllegalStateException();
		}
	}
	
	@FXML
	public final void chooseGame() {
		throw new UnsupportedOperationException();
	}

	@FXML
	public final void defaultGame() {
		throw new UnsupportedOperationException();
	}

	@FXML
	public final void goBack() {
		context.goBack();
	}

	@FXML
	public final void startGame() {
		throw new UnsupportedOperationException();
	}

	@FXML void onGameTypeDomination() {
		configProperty.get().setGameType(GameConfig.Type.DOMINATION);
	}
	
	@FXML void onGameTypeMission() {
		configProperty.get().setGameType(GameConfig.Type.MISSION);
	}
	
	@FXML void  onGameTypeCapital() {
		configProperty.get().setGameType(GameConfig.Type.CAPITAL);
	}
	
	@FXML void onCardStyleFixed() {
		configProperty.get().setCardStyle(GameConfig.CardStyle.FIXED);
	}

	@FXML void onCardStyleIncreasing() {
		configProperty.get().setCardStyle(GameConfig.CardStyle.INCREASING);
	}
	
	@FXML void onCardStyleItalian() {
		configProperty.get().setCardStyle(GameConfig.CardStyle.ITALIAN);
	}
	@FXML private ImageView previewImage;
	
	@FXML
	private CheckBox autoAssignBtn;

	@FXML
	private CheckBox autoPlaceBtn;

	@FXML
	private RadioButton capitalBtn;

	@FXML
	private ToggleGroup cardStyleToggleGroup;

	@FXML
	private RadioButton cardsFixedBtn;

	@FXML
	private RadioButton cardsIncreasingBtn;

	@FXML
	private RadioButton cardsItalianBtn;

	@FXML
	private Button chooseGameBtn;

	@FXML
	private Button defaultGameBtn;

	@FXML
	private RadioButton dominationBtn;

	@FXML
	private ToggleGroup gameTypeToggleGroup;

	@FXML
	private RadioButton missionBtn;

	@FXML
	private Button navIconBtn;

	@FXML
	private ListView<PlayerAssociation> playersList;

	@FXML
	private Button startBtn;

}
