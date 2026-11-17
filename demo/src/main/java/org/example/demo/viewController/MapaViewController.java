package org.example.demo.viewController;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.example.pro_final_ed.controller.ModelFactoryController;
import org.example.pro_final_ed.model.Apartamento;
import org.example.pro_final_ed.model.Estructuras.Grafo;
import org.example.pro_final_ed.model.Estructuras.ListaDoble;

import java.util.HashMap;
import java.util.Map;

public class MapaViewController {

    @FXML private Pane paneMapa;
    @FXML private TextField txtOrigenRuta;
    @FXML private TextField txtDestinoRuta;
    @FXML private TextField txtCierreA;
    @FXML private TextField txtCierreB;
    @FXML private TextArea txtResultadoMapa;

    private final ModelFactoryController mfc = ModelFactoryController.getInstance();
    private final Map<String, double[]> coords = new HashMap<>();
    private ListaDoble<String> rutaResaltada = new ListaDoble<>();

    @FXML
    public void initialize() {
        configurarCoordenadas();
        dibujarGrafo();
    }

    private void configurarCoordenadas() {
        coords.put("Recepcion", new double[]{100, 390});
        coords.put("CuartoAseo", new double[]{100, 230});
        coords.put("EscalerasP1", new double[]{280, 350});
        coords.put("101", new double[]{460, 350});
        coords.put("102", new double[]{630, 350});
        coords.put("EscalerasP2", new double[]{280, 250});
        coords.put("201", new double[]{460, 250});
        coords.put("202", new double[]{630, 250});
    }

    @FXML
    public void dibujarGrafo() {
        paneMapa.getChildren().clear();
        Grafo<String> grafo = mfc.getHotel().getMapaEdificio();

        Map<Integer, Integer> aptosPorPiso = new HashMap<>();
        for (String v : grafo) {
            if (!coords.containsKey(v)) {
                if (v.startsWith("EscalerasP")) {
                    int piso = Integer.parseInt(v.replace("EscalerasP", ""));
                    double y = Math.max(45, 350 - ((piso - 1) * 100));
                    coords.put(v, new double[]{280, y});
                } else {
                    Apartamento apto = mfc.getHotel().buscarApartamento(v);
                    int piso = (apto != null) ? apto.getNumPiso() : 3;
                    int idxEnPiso = aptosPorPiso.getOrDefault(piso, 0);
                    aptosPorPiso.put(piso, idxEnPiso + 1);

                    double y = Math.max(45, 350 - ((piso - 1) * 100));
                    double x = 460 + (idxEnPiso * 170);
                    coords.put(v, new double[]{x, y});
                }
            }
        }

        for (String u : grafo) {
            double[] p1 = coords.get(u);
            for (String v : grafo.obtenerAdyacentes(u)) {
                double[] p2 = coords.get(v);
                if (p1 != null && p2 != null) {
                    Line line = new Line(p1[0], p1[1], p2[0], p2[1]);
                    boolean enRuta = estaAristaEnRuta(u, v);
                    line.setStroke(enRuta ? Color.DARKORANGE : Color.SLATEGRAY);
                    line.setStrokeWidth(enRuta ? 4.5 : 2.0);
                    paneMapa.getChildren().add(line);
                }
            }
        }

        for (String v : grafo) {
            double[] p = coords.get(v);
            if (p == null) continue;

            Color fill = Color.LIGHTSTEELBLUE;
            Apartamento apto = mfc.getHotel().buscarApartamento(v);
            if (apto != null) {
                fill = switch (apto.getEstadoOperativo()) {
                    case PREPARADO -> Color.LIGHTGREEN;
                    case OCUPADO -> Color.DEEPSKYBLUE;
                    case PENDENTE_DE_PREPARACION, EN_PREPARACION -> Color.GOLD;
                    case FUERA_DE_SERVICO -> Color.SALMON;
                };
            } else if (v.toUpperCase().contains("RECEPCION")) {
                fill = Color.MEDIUMPURPLE;
            } else if (v.toUpperCase().contains("ASEO")) {
                fill = Color.LIGHTCYAN;
            }

            Circle circle = new Circle(p[0], p[1], 24, fill);
            circle.setStroke(Color.BLACK);
            circle.setStrokeWidth(1.8);

            Text label = new Text(p[0] - 28, p[1] + 38, v);
            label.setFont(Font.font("System", FontWeight.BOLD, 11));

            paneMapa.getChildren().addAll(circle, label);
        }
    }

    private boolean estaAristaEnRuta(String a, String b) {
        if (rutaResaltada == null || rutaResaltada.getTam() < 2) return false;
        String prev = null;
        for (String curr : rutaResaltada) {
            if (prev != null) {
                if ((prev.equals(a) && curr.equals(b)) || (prev.equals(b) && curr.equals(a))) {
                    return true;
                }
            }
            prev = curr;
        }
        return false;
    }

    @FXML
    public void onCalcularRuta() {
        try {
            String o = txtOrigenRuta.getText().trim();
            String d = txtDestinoRuta.getText().trim();
            rutaResaltada = mfc.getHotel().getMapaEdificio().dijkstraRutaMasCorta(o, d);
            dibujarGrafo();
            if (rutaResaltada.getTam() == 0) {
                txtResultadoMapa.setText("No existe camino entre " + o + " y " + d);
            } else {
                txtResultadoMapa.setText("Ruta más corta (" + o + " -> " + d + "): " + rutaResaltada);
            }
        } catch (Exception e) {
            new Alert(Alert.AlertType.WARNING, e.getMessage()).showAndWait();
        }
    }

    @FXML
    public void onAgruparBloques() {
        try {
            String agrupacion = mfc.agruparPorBloque("Recepcion");
            ListaDoble<String> dfsOrden = mfc.getHotel().getMapaEdificio().recorridoDFS("Recepcion");
            txtResultadoMapa.setText("=== AGRUPACIÓN POR BLOQUE (BFS) ===\n" + agrupacion + "Recorrido DFS desde Recepcion: " + dfsOrden);
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    @FXML
    public void onSimularCierrePasillo() {
        try {
            String a = txtCierreA.getText().trim();
            String b = txtCierreB.getText().trim();
            if (a.isEmpty() || b.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Ingresa ambos extremos del pasillo a simular.").showAndWait();
                return;
            }
            ListaDoble<String> aislados = mfc.simularCierrePasillo("Recepcion", a, b);
            ListaDoble<String> dfsOrden = mfc.getHotel().getMapaEdificio().recorridoDFS("Recepcion");
            if (aislados.getTam() == 0) {
                txtResultadoMapa.setText("Simulación cierre [" + a + " <-> " + b + "]: Todos los nodos siguen accesibles desde Recepcion.\nRecorrido DFS actual: " + dfsOrden);
            } else {
                txtResultadoMapa.setText("¡ALERTA! Si se cierra el pasillo [" + a + " <-> " + b + "], quedan incomunicados: " + aislados);
            }
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }
}
