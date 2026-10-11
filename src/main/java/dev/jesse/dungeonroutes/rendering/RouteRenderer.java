package dev.jesse.dungeonroutes.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.jesse.dungeonroutes.ClientState;
import dev.jesse.dungeonroutes.config.ConfigManager;
import dev.jesse.dungeonroutes.config.ModConfig;
import dev.jesse.dungeonroutes.model.Int3;
import dev.jesse.dungeonroutes.rooms.CoordinateTransformer;
import dev.jesse.dungeonroutes.rooms.RoomMatch;
import dev.jesse.dungeonroutes.routes.RouteWaypoint;
import dev.jesse.dungeonroutes.routes.SecretRoute;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class RouteRenderer {
    private record Segment(float x1, float y1, float z1, float x2, float y2, float z2, int color, float width) {}
    private record Box(float x, float y, float z, float size, int color) {}
    private final ClientState state;

    public RouteRenderer(ClientState state) { this.state = state; }

    public void init() { LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(this::render); }

    private void render(LevelRenderContext context) {
        ModConfig config = ConfigManager.get();
        RoomMatch room = state.room();
        SecretRoute route = state.progress().route();
        if (!config.enabled || !config.renderRoutes || room == null || route == null) return;
        List<RouteWaypoint> points = route.waypoints();
        if (points.isEmpty()) return;

        List<Segment> segments = new ArrayList<>();
        List<Box> boxes = new ArrayList<>();
        int start = config.renderCompleted ? 0 : state.progress().index();
        Int3 previous = null;
        for (int i = start; i < points.size(); i++) {
            Int3 world = CoordinateTransformer.localToWorld(points.get(i).position(), room.origin(), room.rotation());
            int color = withOpacity(i == state.progress().index() ? config.nextWaypointColor : config.waypointColor, config.opacity);
            if (config.showWaypoints) boxes.add(new Box(world.x() + 0.5f, world.y() + 0.5f, world.z() + 0.5f, i == state.progress().index() ? 0.72f : 0.48f, color));
            if (previous != null) segments.add(new Segment(previous.x() + 0.5f, previous.y() + 0.5f, previous.z() + 0.5f,
                world.x() + 0.5f, world.y() + 0.5f, world.z() + 0.5f, withOpacity(config.lineColor, config.opacity), 2.5f));
            if (config.showWaypointNumbers) addNumber(segments, world, i + 1, color);
            previous = world;
        }
        if (segments.isEmpty() && boxes.isEmpty()) return;

        Minecraft client = Minecraft.getInstance();
        Vec3 camera = client.gameRenderer.getMainCamera().position();
        PoseStack poseStack = context.poseStack();
        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);
        PoseStack.Pose pose = poseStack.last();
        MultiBufferSource.BufferSource buffers = context.bufferSource();
        drawBoxes(buffers, pose, boxes);
        drawSegments(buffers, pose, segments);
        poseStack.popPose();
    }

    private void drawSegments(MultiBufferSource.BufferSource buffers, PoseStack.Pose pose, List<Segment> segments) {
        RenderType type = RenderTypes.lines();
        VertexConsumer out = buffers.getBuffer(type);
        for (Segment s : segments) {
            float nx = s.x2 - s.x1, ny = s.y2 - s.y1, nz = s.z2 - s.z1;
            float length = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
            if (length < 0.0001f) continue;
            nx /= length; ny /= length; nz /= length;
            out.addVertex(pose, s.x1, s.y1, s.z1).setColor(s.color).setNormal(pose, nx, ny, nz).setLineWidth(s.width);
            out.addVertex(pose, s.x2, s.y2, s.z2).setColor(s.color).setNormal(pose, nx, ny, nz).setLineWidth(s.width);
        }
        buffers.endBatch(type);
    }

    private void drawBoxes(MultiBufferSource.BufferSource buffers, PoseStack.Pose pose, List<Box> boxes) {
        RenderType type = RenderTypes.debugFilledBox();
        VertexConsumer out = buffers.getBuffer(type);
        for (Box b : boxes) {
            float h = b.size / 2;
            float x0 = b.x - h, x1 = b.x + h, y0 = b.y - h, y1 = b.y + h, z0 = b.z - h, z1 = b.z + h;
            quad(out, pose, b.color, x0,y0,z0, x0,y1,z0, x1,y1,z0, x1,y0,z0);
            quad(out, pose, b.color, x1,y0,z1, x1,y1,z1, x0,y1,z1, x0,y0,z1);
            quad(out, pose, b.color, x0,y0,z1, x0,y1,z1, x0,y1,z0, x0,y0,z0);
            quad(out, pose, b.color, x1,y0,z0, x1,y1,z0, x1,y1,z1, x1,y0,z1);
            quad(out, pose, b.color, x0,y1,z0, x0,y1,z1, x1,y1,z1, x1,y1,z0);
            quad(out, pose, b.color, x0,y0,z1, x0,y0,z0, x1,y0,z0, x1,y0,z1);
        }
        buffers.endBatch(type);
    }

    private void quad(VertexConsumer out, PoseStack.Pose pose, int color, float... v) {
        for (int i = 0; i < v.length; i += 3) out.addVertex(pose, v[i], v[i + 1], v[i + 2]).setColor(color);
    }

    // Compact seven-segment numerals laid flat above a marker; avoids per-frame font allocations.
    private void addNumber(List<Segment> out, Int3 p, int number, int color) {
        String digits = Integer.toString(number);
        float baseX = p.x() + 0.5f - digits.length() * 0.18f;
        float y = p.y() + 1.35f;
        for (int i = 0; i < digits.length(); i++) addDigit(out, baseX + i * 0.36f, y, p.z() + 0.5f, digits.charAt(i) - '0', color);
    }

    private void addDigit(List<Segment> out, float x, float y, float z, int digit, int color) {
        int[] masks = {0x3F,0x06,0x5B,0x4F,0x66,0x6D,0x7D,0x07,0x7F,0x6F};
        float w=.22f, h=.32f;
        float[][] e={{0,0,w,0},{w,0,w,h},{w,h,w,2*h},{0,2*h,w,2*h},{0,h,0,2*h},{0,0,0,h},{0,h,w,h}};
        for(int i=0;i<7;i++) if((masks[digit]&(1<<i))!=0) out.add(new Segment(x+e[i][0],y,z+e[i][1],x+e[i][2],y,z+e[i][3],color,2));
    }

    private int withOpacity(int argb, float opacity) {
        int alpha = Math.max(0, Math.min(255, Math.round(255 * opacity)));
        return (alpha << 24) | (argb & 0xFFFFFF);
    }
}
