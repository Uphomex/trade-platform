# 05-Git 与 GitHub：从本地到远程

## 我们在本仓库实际做了什么

在 `trade-platform/` 目录内（本教学项目的根）执行了：

1. `git init` —— 初始化一个新的 Git 仓库。  
2. 编写 `.gitignore` —— 忽略 `target/`、`node_modules/`、`dist/`、IDE 配置等不应提交的文件。  
3. `git add .` —— 将当前版本的所有项目文件加入暂存区。  
4. `git commit -m "<中文说明本次交付>"` —— 形成第一次提交快照。

> 说明：你的上层工作区 `E:\Git-Local-repository` 可能还存在其他项目（例如别的目录里已有 `.git`）。本交付物将 **trade-platform** 作为**独立可推送**的项目根，避免与无关历史混杂。

## 在 GitHub 上新建空仓库（网页操作）

1. 登录 GitHub → 右上角 **New repository**。  
2. Repository name 例如：`trade-platform`。  
3. 选择 **Public** 或 **Private**；**不要**勾选「Initialize with README」（避免与本地已有提交冲突）。  
4. 创建后页面会给出远程地址，形如：  
   `https://github.com/<你的用户名>/trade-platform.git`

## 绑定远程并推送（命令行）

在 `trade-platform` 目录下执行（把 URL 换成你的）：

```powershell
git remote add origin https://github.com/<你的用户名>/trade-platform.git
git branch -M main
git push -u origin main
```

若使用 SSH：

```powershell
git remote add origin git@github.com:<你的用户名>/trade-platform.git
git push -u origin main
```

## 使用 GitHub CLI `gh`（可选）

若本机已安装 [`gh`](https://cli.github.com/) 且已完成 `gh auth login`，可尝试一键创建并推送：

```powershell
cd trade-platform
gh repo create trade-platform --private --source=. --remote=origin --push
```

若命令报错（未登录、无权限、网络问题），请回到上一节的「网页创建 + git remote + push」流程即可。

## 常用 Git 概念速查

| 命令 | 含义 |
|------|------|
| `git status` | 查看哪些文件被修改、是否在暂存区 |
| `git diff` | 查看工作区与暂存区差异 |
| `git add -p` | 交互式挑选片段加入暂存区（进阶） |
| `git commit --amend` | 修改最近一次提交说明（尚未推送时） |
| `git checkout -b feature/xxx` | 新建并切换到功能分支 |

## 与 `.gitignore` 的配合

Java 编译产物、`node_modules`、构建输出 `dist/` 体积大且可重现，**不要提交**。若误提交，可用 `git rm -r --cached <路径>` 从索引移除并再提交一次修正。
