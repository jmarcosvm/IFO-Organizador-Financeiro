// PROVISÓRIO (Parte 1): só impede o envio nativo do formulário e liga o botão de tema.
// A Parte 2 troca isto pela lógica real (api.js + auth.js).
import { criarBotaoTema } from "../tema.js";
import { toast } from "../ui.js";

document.body.append(criarBotaoTema("tema-flutuante"));

document.getElementById("form-cadastro").addEventListener("submit", (e) => {
  e.preventDefault(); // sem isto o navegador recarregaria a página com a senha na URL
  toast("O cadastro será ligado à API na Parte 2 da R4.", "info");
});
