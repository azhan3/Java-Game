package scenes;

import Map.NewMap;
import components.*;
import imgui.ImGui;
import imgui.ImVec2;
import engine.*;
import org.joml.Vector2f;
import physics2d.components.Box2DCollider;
import physics2d.components.Rigidbody2D;
import physics2d.enums.BodyType;
import renderer.Renderer;
import util.AssetPool;

public class LevelEditorScene extends Scene {
    private Sprite selected;
    private GameObject obj1;
    private Spritesheet sprites, blocks;
    SpriteRenderer obj1Sprite;

    GameObject levelEditorStuff = new GameObject("LevelEditor", new Transform(new Vector2f()), 0);

    public LevelEditorScene() {

    }

    @Override
    public void init() {
        levelEditorStuff.addComponent(new MouseControls());
        levelEditorStuff.addComponent(new GridLines());

        loadResources();
        this.camera = new Camera(new Vector2f(0, 0));
        sprites = AssetPool.getSpritesheet("assets/images/spritesheets/spritesheet.png");
        blocks = AssetPool.getSpritesheet("assets/images/spritesheets/blocks.png");
        if (levelLoaded) {
            this.activeGameObject = gameObjects.get(0);
            return;
        }

        //new NewMap();
        System.out.println(Window.getScene().getGameObjects().size());

//        obj1 = new GameObject("Object 1", new Transform(new Vector2f(200, 100),
//                new Vector2f(256, 256)), 2);
//        obj1Sprite = new SpriteRenderer();
//        obj1Sprite.setColor(new Vector4f(1, 0, 0, 0.5f));
//        obj1.addComponent(obj1Sprite);
//        obj1.addComponent(new Rigidbody());
//        this.addGameObjectToScene(obj1);
//        this.activeGameObject = obj1;
//
//        GameObject obj2 = new GameObject("Object 2",
//                new Transform(new Vector2f(400, 100), new Vector2f(256, 256)), 1);
//        SpriteRenderer obj2SpriteRenderer = new SpriteRenderer();
//        Sprite obj2Sprite = new Sprite();
//        obj2Sprite.setTexture(AssetPool.getTexture("assets/images/Sunplate_Block.png"));
//        obj2SpriteRenderer.setSprite(obj2Sprite);
//        obj2.addComponent(obj2SpriteRenderer);
//        this.addGameObjectToScene(obj2);
    }

    private void loadResources() {
        AssetPool.getShader("assets/shaders/default.glsl");
        Renderer.bindShader(AssetPool.getShader("assets/shaders/default.glsl"));
        AssetPool.addSpritesheet("assets/images/spritesheets/spritesheet.png",
                new Spritesheet(AssetPool.getTexture("assets/images/spritesheets/spritesheet.png"),
                        48, 48, 2, 0));
        AssetPool.addSpritesheet("assets/images/spritesheets/blocks.png",
                new Spritesheet(AssetPool.getTexture("assets/images/spritesheets/blocks.png"),
                        16, 16, 30, 0));
        AssetPool.addSpritesheet("assets/images/spritesheets/player_walk.png", new Spritesheet(AssetPool.getTexture("assets/images/spritesheets/player_walk.png"), 43, 60, 10, 0));
        AssetPool.addSpritesheet("assets/images/spritesheets/player_swing.png", new Spritesheet(AssetPool.getTexture("assets/images/spritesheets/player_swing.png"), 43, 60, 10, 0));

        AssetPool.getTexture("assets/images/Dirt_Block.png");
        AssetPool.getTexture("assets/images/IdleCharacter.png");
        AssetPool.getTexture("assets/images/JumpCharacter.png");

        for (GameObject g : this.getGameObjects()) {
            if (g.getComponent(SpriteRenderer.class) != null) {
                SpriteRenderer spr = g.getComponent(SpriteRenderer.class);
                if (spr.getTexture() != null) {
                    spr.setTexture(AssetPool.getTexture(spr.getTexture().getFilepath()));
                }
            }

            if (g.getComponent(StateMachine.class) != null) {
                StateMachine stateMachine = g.getComponent(StateMachine.class);
                stateMachine.refreshTextures();
            }
        }
    }

    @Override
    public void update(float dt) {
        levelEditorStuff.update(dt);

        for (GameObject go : this.gameObjects) {
            go.update(dt);
        }
        this.camera.adjustProjection();
        this.physics2D.update(dt);

        for (int i=0; i < gameObjects.size(); i++) {
            GameObject go = gameObjects.get(i);
            go.update(dt);

            if (go.isDead()) {
                gameObjects.remove(i);
                this.renderer.destroyGameObject(go);
                this.physics2D.destroyGameObject(go);
                i--;
            }
        }

        this.renderer.render();
    }

    @Override
    public void imgui() {

        ImGui.begin("Inventory");

        ImVec2 windowPos = new ImVec2();
        ImGui.getWindowPos(windowPos);
        ImVec2 windowSize = new ImVec2();
        ImGui.getWindowSize(windowSize);
        ImVec2 itemSpacing = new ImVec2();
        ImGui.getStyle().getItemSpacing(itemSpacing);

        float windowX2 = windowPos.x + windowSize.x;
        for (int i=0; i < blocks.size(); i++) {
            Sprite sprite = blocks.getSprite(i);
            float spriteWidth = sprite.getWidth() ;
            float spriteHeight = sprite.getHeight() ;
            int id = sprite.getTexId();
            Vector2f[] texCoords = sprite.getTexCoords();

            ImGui.pushID(i);
            if (ImGui.imageButton(id, spriteWidth, spriteHeight, texCoords[0].x, texCoords[0].y, texCoords[2].x, texCoords[2].y)) {
                GameObject object = Prefabs.generateSpriteObject(sprite, 16, 16);
                Rigidbody2D rb = new Rigidbody2D();
                rb.setBodyType(BodyType.Static);
                object.addComponent(rb);
                Box2DCollider b2d = new Box2DCollider();

                b2d.setHalfSize(new Vector2f(16, 16));
                b2d.setOffset(new Vector2f(0,0));
                object.addComponent(b2d);
                object.addComponent(new Ground());

                levelEditorStuff.getComponent(MouseControls.class).pickupObject(object);
            }
            ImGui.popID();

            ImVec2 lastButtonPos = new ImVec2();
            ImGui.getItemRectMax(lastButtonPos);
            float lastButtonX2 = lastButtonPos.x;
            float nextButtonX2 = lastButtonX2 + itemSpacing.x + spriteWidth;
            if (i + 1 < blocks.size() && nextButtonX2 < windowX2) {
                ImGui.sameLine();
            }
        }
        Spritesheet playerSprites = AssetPool.getSpritesheet("assets/images/spritesheets/player_walk.png");
        Sprite sprite = playerSprites.getSprite(0);
        float spriteWidth = sprite.getWidth() * 4;
        float spriteHeight = sprite.getHeight() * 4;
        int id = sprite.getTexId();
        Vector2f[] texCoords = sprite.getTexCoords();

        if (ImGui.imageButton(id, spriteWidth, spriteHeight, texCoords[2].x, texCoords[0].y, texCoords[0].x, texCoords[2].y)) {
            GameObject plr = Prefabs.generatePlayer();
            levelEditorStuff.getComponent(MouseControls.class).pickupObject(plr);
        }


        ImGui.end();
    }
}