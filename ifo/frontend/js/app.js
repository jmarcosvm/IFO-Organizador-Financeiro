/* ============================================
   IFO - JavaScript Principal
   CRUD COMPLETO - CONECTADO COM BACKEND
   ============================================ */

// URL da API Backend
const API_URL = "http://localhost:8081/api";

// Dados do usuário logado
let usuarioLogado = null;

/* ============================================
   FUNÇÕES DE API (Chamadas ao Backend)
   ============================================ */

// Buscar transações do usuário
async function buscarTransacoes(idUsuario) {
    try {
        const response = await fetch(`${API_URL}/transacoes?usuario=${idUsuario}`);
        if (response.ok) {
            return await response.json();
        }
        return [];
    } catch (error) {
        console.error("Erro ao buscar transações:", error);
        return [];
    }
}

// Buscar metas do usuário
async function buscarMetas(idUsuario) {
    try {
        const response = await fetch(`${API_URL}/metas?usuario=${idUsuario}`);
        if (response.ok) {
            return await response.json();
        }
        return [];
    } catch (error) {
        console.error("Erro ao buscar metas:", error);
        return [];
    }
}

// Buscar orçamentos do usuário
async function buscarOrcamentos(idUsuario) {
    try {
        const response = await fetch(`${API_URL}/orcamentos?usuario=${idUsuario}`);
        if (response.ok) {
            return await response.json();
        }
        return [];
    } catch (error) {
        console.error("Erro ao buscar orçamentos:", error);
        return [];
    }
}

// Buscar investimentos do usuário
async function buscarInvestimentos(idUsuario) {
    try {
        const response = await fetch(`${API_URL}/investimentos?usuario=${idUsuario}`);
        if (response.ok) {
            return await response.json();
        }
        return [];
    } catch (error) {
        console.error("Erro ao buscar investimentos:", error);
        return [];
    }
}

// Calcular dashboard
async function buscarDashboard(idUsuario) {
    try {
        const response = await fetch(`${API_URL}/transacoes/saldo?usuario=${idUsuario}`);
        if (response.ok) {
            const saldo = await response.json();
            const transacoes = await buscarTransacoes(idUsuario);
            const receitas = transacoes.filter(t => t.tipo === 'RECEITA').reduce((sum, t) => sum + t.valor, 0);
            const despesas = transacoes.filter(t => t.tipo === 'DESPESA').reduce((sum, t) => sum + t.valor, 0);
            return { saldo, totalReceitas: receitas, totalDespesas: despesas };
        }
        return { saldo: 0, totalReceitas: 0, totalDespesas: 0 };
    } catch (error) {
        console.error("Erro ao buscar dashboard:", error);
        return { saldo: 0, totalReceitas: 0, totalDespesas: 0 };
    }
}

// Login
async function loginUsuario(email, senha) {
    try {
        const response = await fetch(`${API_URL}/usuarios/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, senha })
        });
        if (response.ok) {
            return await response.json();
        }
        return null;
    } catch (error) {
        console.error("Erro no login:", error);
        return null;
    }
}

// Cadastro
async function cadastrarUsuario(nome, email, senha, rendaMensal) {
    try {
        const response = await fetch(`${API_URL}/usuarios`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ nome, email, senha, rendaMensal })
        });
        if (response.ok) {
            return await response.json();
        }
        return null;
    } catch (error) {
        console.error("Erro no cadastro:", error);
        return null;
    }
}

// CRIAR TRANSAÇÃO
async function criarTransacao(transacao) {
    try {
        const response = await fetch(`${API_URL}/transacoes`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(transacao)
        });
        if (response.ok) {
            return await response.json();
        }
        return null;
    } catch (error) {
        console.error("Erro ao criar transação:", error);
        return null;
    }
}

// EDITAR TRANSAÇÃO
async function editarTransacaoAPI(id, transacao) {
    try {
        await fetch(`${API_URL}/transacoes/${id}`, { method: 'DELETE' });
        transacao.idTransacao = null;
        return await criarTransacao(transacao);
    } catch (error) {
        console.error("Erro ao editar transação:", error);
        return null;
    }
}

// DELETAR TRANSAÇÃO
async function deletarTransacaoAPI(id) {
    try {
        const response = await fetch(`${API_URL}/transacoes/${id}`, { method: 'DELETE' });
        return response.ok;
    } catch (error) {
        console.error("Erro ao deletar transação:", error);
        return false;
    }
}

// CRIAR META
async function criarMeta(meta) {
    try {
        const response = await fetch(`${API_URL}/metas`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(meta)
        });
        if (response.ok) {
            return await response.json();
        }
        return null;
    } catch (error) {
        console.error("Erro ao criar meta:", error);
        return null;
    }
}

// CRIAR ORÇAMENTO
async function criarOrcamento(orcamento) {
    try {
        const response = await fetch(`${API_URL}/orcamentos`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(orcamento)
        });
        if (response.ok) {
            return await response.json();
        }
        return null;
    } catch (error) {
        console.error("Erro ao criar orçamento:", error);
        return null;
    }
}

// CRIAR INVESTIMENTO
async function criarInvestimento(investimento) {
    try {
        const response = await fetch(`${API_URL}/investimentos`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(investimento)
        });
        if (response.ok) {
            return await response.json();
        }
        return null;
    } catch (error) {
        console.error("Erro ao criar investimento:", error);
        return null;
    }
}

/* ============================================
   FUNÇÕES UTILITÁRIAS
   ============================================ */

function formatarMoeda(valor) {
    return valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
}

function formatarData(dataISO) {
    if (!dataISO) return "-";
    const [ano, mes, dia] = dataISO.split("-");
    return `${dia}/${mes}/${ano}`;
}

function formatarDataInput(data) {
    if (!data) return "";
    const [dia, mes, ano] = data.split("/");
    return `${ano}-${mes}-${dia}`;
}

function calcularProgresso(meta) {
    return ((meta.valorAtual / meta.valorObjetivo) * 100).toFixed(1);
}

/* ============================================
   FUNÇÕES DE RENDERIZAÇÃO
   ============================================ */

function carregarUsuario() {
    const usuarioElement = document.querySelector("#nome-usuario");
    if (usuarioElement && usuarioLogado) {
        usuarioElement.textContent = usuarioLogado.nome;
    }
}

async function carregarDashboard() {
    if (!usuarioLogado) return;

    const dashboard = await buscarDashboard(usuarioLogado.idUsuario);

    const renda = usuarioLogado.rendaMensal || 0;
    const necessidades = renda * 0.50;
    const lazer = renda * 0.30;
    const investimentos = renda * 0.20;

    const saldoElement = document.querySelector("#saldo");
    const receitasElement = document.querySelector("#total-receitas");
    const despesasElement = document.querySelector("#total-despesas");
    const necessidadesElement = document.querySelector("#necessidades-valor");
    const lazerElement = document.querySelector("#lazer-valor");
    const investimentosElement = document.querySelector("#investimentos-valor");

    if (necessidadesElement) necessidadesElement.textContent = formatarMoeda(necessidades);
    if (lazerElement) lazerElement.textContent = formatarMoeda(lazer);
    if (investimentosElement) investimentosElement.textContent = formatarMoeda(investimentos);
    if (saldoElement) saldoElement.textContent = formatarMoeda(dashboard.saldo);
    if (receitasElement) receitasElement.textContent = formatarMoeda(dashboard.totalReceitas);
    if (despesasElement) despesasElement.textContent = formatarMoeda(dashboard.totalDespesas);
}

async function carregarTransacoes() {
    const tbody = document.querySelector("#tabela-transacoes");
    if (!tbody || !usuarioLogado) return;

    const transacoes = await buscarTransacoes(usuarioLogado.idUsuario);

    tbody.innerHTML = transacoes.map(t => `
        <tr>
            <td>${formatarData(t.data)}</td>
            <td>${t.descricao}</td>
            <td><span class="badge ${t.tipo === 'RECEITA' ? 'badge-receita' : 'badge-despesa'}">${t.tipo === 'RECEITA' ? 'Receita' : 'Despesa'}</span></td>
            <td class="${t.tipo === 'RECEITA' ? 'receita' : 'despesa'}">${t.tipo === 'RECEITA' ? '+' : '-'} ${formatarMoeda(t.valor)}</td>
            <td>
                <button class="btn btn-sm btn-secondary" onclick="mostrarFormEditarTransacao(${t.idTransacao})">Editar</button>
                <button class="btn btn-sm btn-danger" onclick="confirmarExclusaoTransacao(${t.idTransacao})">Excluir</button>
            </td>
        </tr>
    `).join("");
}

async function carregarMetas() {
    const container = document.querySelector("#lista-metas");
    if (!container || !usuarioLogado) return;

    const metas = await buscarMetas(usuarioLogado.idUsuario);

    if (metas.length === 0) {
        container.innerHTML = '<p class="text-center">Nenhuma meta cadastrada.</p>';
        return;
    }

    container.innerHTML = metas.map(meta => {
        const progresso = calcularProgresso(meta);
        const classeProgresso = progresso >= 100 ? 'progress-green' : progresso >= 50 ? 'progress-yellow' : 'progress-red';
        return `
            <div class="card">
                <h3>${meta.tituloMeta}</h3>
                <div class="flex mb-2">
                    <span>${formatarMoeda(meta.valorAtual)}</span>
                    <span>de ${formatarMoeda(meta.valorObjetivo)}</span>
                </div>
                <div class="progress-container">
                    <div class="progress-bar ${classeProgresso}" style="width: ${progresso}%"></div>
                </div>
                <p class="text-center mt-2"><strong>${progresso}%</strong> concluído</p>
                <p class="text-center text-muted">Prazo: ${formatarData(meta.prazo)}</p>
                <div class="flex mt-2" style="justify-content: flex-end;">
                    <button class="btn btn-sm btn-danger" onclick="confirmarExclusaoMeta(${meta.idMeta})">Excluir</button>
                </div>
            </div>
        `;
    }).join("");
}

async function excluirOrcamento(id) {
    if (!confirm("Deseja realmente excluir este orçamento?")) return;
    try {
        const response = await fetch(`${API_URL}/orcamentos/${id}`, { method: "DELETE" });
        if (response.ok) {
            carregarOrcamentos();
        } else {
            alert("Erro ao excluir orçamento.");
        }
    } catch (error) {
        console.error("Erro ao excluir orçamento:", error);
        alert("Erro ao excluir orçamento.");
    }
}

async function carregarOrcamentos() {
    const container = document.querySelector("#lista-orcamentos");
    if (!container || !usuarioLogado) return;

    const orcamentos = await buscarOrcamentos(usuarioLogado.idUsuario);

    if (orcamentos.length === 0) {
        const renda = usuarioLogado.rendaMensal || 0;
        const padrao = [
            { tipoOrcamento: "NECESSIDADES", percentual: 0.50 },
            { tipoOrcamento: "LAZER",        percentual: 0.30 },
            { tipoOrcamento: "INVESTIMENTOS",percentual: 0.20 }
        ];
        const dataAtual = new Date();
        for (const cat of padrao) {
            await criarOrcamento({
                idUsuario: usuarioLogado.idUsuario,
                tipoOrcamento: cat.tipoOrcamento,
                limiteValor: renda * cat.percentual,
                mes: dataAtual.getMonth() + 1,
                ano: dataAtual.getFullYear()
            });
        }
        const orcamentosAtualizados = await buscarOrcamentos(usuarioLogado.idUsuario);
        renderizarOrcamentos(orcamentosAtualizados, container);
    } else {
        renderizarOrcamentos(orcamentos, container);
    }
}

function renderizarOrcamentos(orcamentos, container) {
    const nomes = {
        NECESSIDADES: "Necessidades",
        LAZER: "Lazer",
        INVESTIMENTOS: "Investimentos"
    };

    container.innerHTML = orcamentos.map(orc => {
        const nomeCategoria = nomes[orc.tipoOrcamento] || orc.tipoOrcamento;
        return `
            <div class="card mb-3" style="display:flex; justify-content:space-between; align-items:center;">
                <div>
                    <h4>Orçamento ${orc.mes}/${orc.ano}</h4>
                    <p><strong>Categoria:</strong> ${nomeCategoria}</p>
                    <p><strong>Limite:</strong> ${formatarMoeda(orc.limiteValor)}</p>
                </div>
                <button class="btn btn-danger" onclick="excluirOrcamento(${orc.idOrcamento})">Excluir</button>
            </div>
        `;
    }).join("");
}

async function carregarInvestimentos() {
    const container = document.querySelector("#resultado-investimento");
    if (!container || !usuarioLogado) return;

    const investimentos = await buscarInvestimentos(usuarioLogado.idUsuario);

    if (investimentos.length === 0) {
        container.innerHTML = '<p class="text-center">Nenhuma simulação cadastrada.</p>';
        return;
    }

    const inv = investimentos[0];
    const totalInvestido = inv.valorMensal * inv.tempoMeses;
    const rendimento = inv.montanteFinal - totalInvestido;

    container.innerHTML = `
        <div class="grid-2">
            <div class="card">
                <h3>Resumo da Simulação</h3>
                <p><strong>Valor Mensal:</strong> ${formatarMoeda(inv.valorMensal)}</p>
                <p><strong>Taxa de Juros:</strong> ${inv.taxaJuros}% ao mês</p>
                <p><strong>Tempo:</strong> ${inv.tempoMeses} meses</p>
                <p><strong>Tipo:</strong> ${inv.tipoJuros === 'COMPOSTO' ? 'Juros Compostos' : 'Juros Simples'}</p>
            </div>
            <div class="card">
                <h3>Resultados</h3>
                <p><strong>Total Investido:</strong> ${formatarMoeda(totalInvestido)}</p>
                <p><strong>Montante Final:</strong> <span style="color: #10B981; font-size: 24px;">${formatarMoeda(inv.montanteFinal)}</span></p>
                <p><strong>Rendimento:</strong> ${formatarMoeda(rendimento)}</p>
                <p><strong>Rentabilidade:</strong> ${((rendimento / totalInvestido) * 100).toFixed(2)}%</p>
            </div>
        </div>
    `;
}

/* ============================================
   FUNÇÕES DE CRUD (MODAIS/FORMULÁRIOS)
   ============================================ */

function mostrarFormNovaTransacao() {
    const html = `
        <div id="modal-transacao" class="modal">
            <div class="modal-content">
                <h3>Nova Transação</h3>
                <form id="form-transacao">
                    <div class="form-group">
                        <label>Tipo</label>
                        <select id="transacao-tipo" class="form-control" required>
                            <option value="RECEITA">Receita</option>
                            <option value="DESPESA">Despesa</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Descrição</label>
                        <input type="text" id="transacao-descricao" class="form-control" required>
                    </div>
                    <div class="form-group">
                        <label>Valor (R$)</label>
                        <input type="number" id="transacao-valor" class="form-control" step="0.01" min="0.01" required>
                    </div>
                    <div class="form-group">
                        <label>Data</label>
                        <input type="date" id="transacao-data" class="form-control" required>
                    </div>
                    <div class="form-group">
                        <label>Categoria</label>
                        <select id="transacao-categoria" class="form-control" required>
                            <option value="1">Mesada</option>
                            <option value="2">Freelance</option>
                            <option value="3">Lazer</option>
                            <option value="4">Alimentação</option>
                            <option value="5">Transporte</option>
                            <option value="6">Investimento</option>
                        </select>
                    </div>
                    <div class="flex">
                        <button type="button" class="btn btn-secondary" onclick="fecharModal()">Cancelar</button>
                        <button type="submit" class="btn btn-success">Salvar</button>
                    </div>
                </form>
            </div>
        </div>
    `;
    document.body.insertAdjacentHTML('beforeend', html);

    document.getElementById('form-transacao').addEventListener('submit', async function(e) {
        e.preventDefault();
        const transacao = {
            idUsuario: usuarioLogado.idUsuario,
            idConta: 1,
            idCategoria: parseInt(document.getElementById('transacao-categoria').value),
            valor: parseFloat(document.getElementById('transacao-valor').value),
            tipo: document.getElementById('transacao-tipo').value,
            data: document.getElementById('transacao-data').value,
            descricao: document.getElementById('transacao-descricao').value,
            paga: true
        };
        const resultado = await criarTransacao(transacao);
        if (resultado) {
            alert('Transação criada com sucesso!');
            fecharModal();
            carregarTransacoes();
            carregarDashboard();
        } else {
            alert('Erro ao criar transação.');
        }
    });
}

async function confirmarExclusaoTransacao(id) {
    if (confirm('Tem certeza que deseja excluir esta transação?')) {
        const sucesso = await deletarTransacaoAPI(id);
        if (sucesso) {
            alert('Transação excluída com sucesso!');
            carregarTransacoes();
            carregarDashboard();
        } else {
            alert('Erro ao excluir transação.');
        }
    }
}

async function mostrarFormEditarTransacao(id) {
    const transacoes = await buscarTransacoes(usuarioLogado.idUsuario);
    const transacao = transacoes.find(t => t.idTransacao === id);
    if (!transacao) return;

    const novaDescricao = prompt('Nova descrição:', transacao.descricao);
    if (novaDescricao === null) return;

    const novoValor = prompt('Novo valor:', transacao.valor);
    if (novoValor === null) return;

    const transacaoEditada = {
        ...transacao,
        descricao: novaDescricao,
        valor: parseFloat(novoValor)
    };

    const resultado = await editarTransacaoAPI(id, transacaoEditada);
    if (resultado) {
        alert('Transação editada com sucesso!');
        carregarTransacoes();
        carregarDashboard();
    } else {
        alert('Erro ao editar transação.');
    }
}

function fecharModal() {
    const modals = document.querySelectorAll('.modal');
    modals.forEach(m => m.remove());
}

function mostrarCamposInvestimento() {
    const tipo = document.getElementById('tipo-investimento')?.value;
    const campoInicial = document.getElementById('campo-aplicacao-inicial');
    const campoMensal = document.getElementById('campo-aporte-mensal');
    if (!tipo || !campoInicial || !campoMensal) return;
    if (tipo === 'unico') {
        campoInicial.style.display = 'block';
        campoMensal.style.display = 'none';
    } else if (tipo === 'mensal') {
        campoInicial.style.display = 'none';
        campoMensal.style.display = 'block';
    } else {
        campoInicial.style.display = 'block';
        campoMensal.style.display = 'block';
    }
}

function simularInvestimento() {
    const tipoInvestimento = document.getElementById('tipo-investimento')?.value || 'ambos';
    const aplicacaoInicial = parseFloat(document.getElementById('aplicacao-inicial')?.value) || 0;
    const valorMensal = parseFloat(document.getElementById('valor-mensal')?.value) || 0;
    const taxaJuros = parseFloat(document.getElementById('taxa-juros')?.value) || 0;
    const tempoMeses = parseInt(document.getElementById('tempo-meses')?.value) || 0;
    const tipoJuros = document.getElementById('tipo-juros')?.value || 'COMPOSTO';

    let montanteFinal = 0;
    let totalInvestido = 0;
    const taxaDecimal = taxaJuros / 100;

    if (tipoJuros === 'COMPOSTO') {
        if (tipoInvestimento === 'unico') {
            montanteFinal = aplicacaoInicial * Math.pow(1 + taxaDecimal, tempoMeses);
            totalInvestido = aplicacaoInicial;
        } else if (tipoInvestimento === 'mensal') {
            montanteFinal = taxaDecimal > 0
                ? valorMensal * ((Math.pow(1 + taxaDecimal, tempoMeses) - 1) / taxaDecimal)
                : valorMensal * tempoMeses;
            totalInvestido = valorMensal * tempoMeses;
        } else {
            const montanteInicial = aplicacaoInicial * Math.pow(1 + taxaDecimal, tempoMeses);
            const montanteAportes = taxaDecimal > 0
                ? valorMensal * ((Math.pow(1 + taxaDecimal, tempoMeses) - 1) / taxaDecimal)
                : valorMensal * tempoMeses;
            montanteFinal = montanteInicial + montanteAportes;
            totalInvestido = aplicacaoInicial + (valorMensal * tempoMeses);
        }
    } else {
        if (tipoInvestimento === 'unico') {
            montanteFinal = aplicacaoInicial * (1 + taxaDecimal * tempoMeses);
            totalInvestido = aplicacaoInicial;
        } else if (tipoInvestimento === 'mensal') {
            const somaMeses = (tempoMeses * (tempoMeses + 1)) / 2;
            montanteFinal = (valorMensal * tempoMeses) + (valorMensal * taxaDecimal * somaMeses);
            totalInvestido = valorMensal * tempoMeses;
        } else {
            const montanteInicial = aplicacaoInicial * (1 + taxaDecimal * tempoMeses);
            const somaMeses = (tempoMeses * (tempoMeses + 1)) / 2;
            const montanteAportes = (valorMensal * tempoMeses) + (valorMensal * taxaDecimal * somaMeses);
            montanteFinal = montanteInicial + montanteAportes;
            totalInvestido = aplicacaoInicial + (valorMensal * tempoMeses);
        }
    }

    const rendimento = montanteFinal - totalInvestido;
    const rentabilidade = totalInvestido > 0 ? (rendimento / totalInvestido) * 100 : 0;
    const descTipo = { unico: 'Aplicação Única', mensal: 'Aporte Mensal', ambos: 'Aplicação + Aporte Mensal' };

    const resultadoDiv = document.querySelector("#resultado-investimento");
    if (resultadoDiv) {
        resultadoDiv.innerHTML = `
            <div class="grid-2">
                <div class="card">
                    <h3>Resumo da Simulação</h3>
                    <p><strong>Tipo:</strong> ${descTipo[tipoInvestimento]}</p>
                    ${tipoInvestimento !== 'mensal' ? `<p><strong>Aplicação Inicial:</strong> ${formatarMoeda(aplicacaoInicial)}</p>` : ''}
                    ${tipoInvestimento !== 'unico' ? `<p><strong>Valor Mensal:</strong> ${formatarMoeda(valorMensal)}</p>` : ''}
                    <p><strong>Taxa de Juros:</strong> ${taxaJuros}% ao mês</p>
                    <p><strong>Tempo:</strong> ${tempoMeses} meses</p>
                    <p><strong>Tipo de Juros:</strong> ${tipoJuros === 'COMPOSTO' ? 'Juros Compostos' : 'Juros Simples'}</p>
                </div>
                <div class="card">
                    <h3>Resultados</h3>
                    <p><strong>Total Investido:</strong> ${formatarMoeda(totalInvestido)}</p>
                    <p><strong>Montante Final:</strong> <span style="color: #10B981; font-size: 24px;">${formatarMoeda(montanteFinal)}</span></p>
                    <p><strong>Rendimento:</strong> ${formatarMoeda(rendimento)}</p>
                    <p><strong>Rentabilidade:</strong> ${rentabilidade.toFixed(2)}%</p>
                </div>
            </div>
        `;
    }
}

async function deletarMetaAPI(id) {
    try {
        const response = await fetch(`${API_URL}/metas/${id}`, { method: 'DELETE' });
        return response.ok;
    } catch (error) {
        console.error("Erro ao deletar meta:", error);
        return false;
    }
}

async function confirmarExclusaoMeta(id) {
    if (confirm('Tem certeza que deseja excluir esta meta?')) {
        const sucesso = await deletarMetaAPI(id);
        if (sucesso) {
            alert('Meta excluída com sucesso!');
            carregarMetas();
        } else {
            alert('Erro ao excluir meta. Verifique se o backend está rodando.');
        }
    }
}

function mostrarFormNovoOrcamento() {
    const hoje = new Date();
    const html = `
        <div id="modal-orcamento" class="modal">
            <div class="modal-content">
                <h3>Novo Orçamento</h3>
                <form id="form-orcamento">
                    <div class="form-group">
                        <label>Categoria</label>
                        <select id="orcamento-categoria" class="form-control" required>
                            <option value="NECESSIDADES">Necessidades</option>
                            <option value="LAZER">Lazer</option>
                            <option value="INVESTIMENTOS">Investimentos</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Limite Mensal (R$)</label>
                        <input type="number" id="orcamento-limite" class="form-control" step="0.01" min="0" required>
                    </div>
                    <div class="form-group">
                        <label>Mês</label>
                        <input type="number" id="orcamento-mes" class="form-control" min="1" max="12" value="${hoje.getMonth() + 1}" required>
                    </div>
                    <div class="form-group">
                        <label>Ano</label>
                        <input type="number" id="orcamento-ano" class="form-control" value="${hoje.getFullYear()}" required>
                    </div>
                    <div class="flex">
                        <button type="button" class="btn btn-secondary" onclick="fecharModal()">Cancelar</button>
                        <button type="submit" class="btn btn-success">Salvar Orçamento</button>
                    </div>
                </form>
            </div>
        </div>
    `;
    document.body.insertAdjacentHTML('beforeend', html);

    document.getElementById('form-orcamento').addEventListener('submit', async function(e) {
        e.preventDefault();
        const orcamento = {
            idUsuario: usuarioLogado.idUsuario,
            tipoOrcamento: document.getElementById('orcamento-categoria').value,
            limiteValor: parseFloat(document.getElementById('orcamento-limite').value),
            mes: parseInt(document.getElementById('orcamento-mes').value),
            ano: parseInt(document.getElementById('orcamento-ano').value)
        };
        const resultado = await criarOrcamento(orcamento);
        if (resultado) {
            alert('Orçamento criado com sucesso!');
            fecharModal();
            carregarOrcamentos();
        } else {
            alert('Erro ao criar orçamento.');
        }
    });
}

function logout() {
    localStorage.removeItem("usuarioLogado");
    window.location.href = "index.html";
}

function mostrarFormNovaMeta() {
    const hoje = new Date().toISOString().split('T')[0];
    const html = `
        <div id="modal-meta" class="modal">
            <div class="modal-content">
                <h3>Nova Meta Financeira</h3>
                <form id="form-meta">
                    <div class="form-group">
                        <label>Título da Meta</label>
                        <input type="text" id="meta-titulo" class="form-control" required placeholder="Ex: Viagem, Reserva de Emergência">
                    </div>
                    <div class="form-group">
                        <label>Valor Objetivo (R$)</label>
                        <input type="number" id="meta-valor" class="form-control" step="0.01" min="0" required>
                    </div>
                    <div class="form-group">
                        <label>Prazo</label>
                        <input type="date" id="meta-prazo" class="form-control" min="${hoje}" required>
                    </div>
                    <div class="flex">
                        <button type="button" class="btn btn-secondary" onclick="fecharModal()">Cancelar</button>
                        <button type="submit" class="btn btn-success">Salvar Meta</button>
                    </div>
                </form>
            </div>
        </div>
    `;
    document.body.insertAdjacentHTML('beforeend', html);

    document.getElementById('form-meta').addEventListener('submit', async function(e) {
        e.preventDefault();
        const meta = {
            idUsuario: usuarioLogado.idUsuario,
            tituloMeta: document.getElementById('meta-titulo').value,
            valorObjetivo: parseFloat(document.getElementById('meta-valor').value),
            valorAtual: 0,
            prazo: document.getElementById('meta-prazo').value
        };
        const resultado = await criarMeta(meta);
        if (resultado) {
            alert('Meta criada com sucesso!');
            fecharModal();
            carregarMetas();
        } else {
            alert('Erro ao criar meta.');
        }
    });
}

/* ============================================
   INICIALIZAÇÃO
   ============================================ */

document.addEventListener("DOMContentLoaded", async () => {
    mostrarCamposInvestimento();

    const usuarioSalvo = localStorage.getItem("usuarioLogado");
    if (usuarioSalvo) {
        usuarioLogado = JSON.parse(usuarioSalvo);
        carregarUsuario();

        if (document.querySelector("#saldo")) await carregarDashboard();
        if (document.querySelector("#tabela-transacoes")) await carregarTransacoes();
        if (document.querySelector("#lista-metas")) await carregarMetas();
        if (document.querySelector("#lista-orcamentos")) await carregarOrcamentos();
        if (document.querySelector("#resultado-investimento")) await carregarInvestimentos();
    }

    console.log("IFO Frontend carregado - CRUD Completo!");
});