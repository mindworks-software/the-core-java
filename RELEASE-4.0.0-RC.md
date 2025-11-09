# Release Candidate 4.0.0 - Testing Summary

**Date:** 2025-10-28
**Branch:** `release-4.0.0`
**JIRA:** [NOVDEV-2114](https://jira.bayportfinance.com:8443/browse/NOVDEV-2114)
**Status:** 🚀 Ready for RC Testing

---

## 🎯 Purpose

This Release Candidate contains comprehensive CI/CD pipeline improvements that enable:
- Automated testing on every commit
- Artifact publishing to Artifactory
- Docker image building and registry publishing
- Full end-to-end pipeline validation

---

## 📦 What Will Be Built

### **Pipeline VERSION:**
```
4.0.0-RC-ff1eb48
```

### **Docker Image:**
```
docker-registry-default.paas.bayportfinance.com/novalend/thecore-router:4.0.0-RC-ff1eb48
```

### **Artifactory Artifacts:**
```
com.korwe:thecore-api:4.0.0-SNAPSHOT.jar
com.korwe:thecore-api:4.0.0-SNAPSHOT-sources.jar
com.korwe:thecore-router:4.0.0-SNAPSHOT.jar
com.korwe:thecore-router:4.0.0-SNAPSHOT-sources.jar
```

---

## 🔄 Pipeline Stages

The release branch will execute all pipeline stages:

### **1. Compile** ✅
- Assembles code
- Compiles test classes
- Creates initial artifacts

### **2. Test** ✅
- Runs unit tests with JUnit
- Tests are **mandatory** (fail pipeline if tests fail)
- Generates test reports

### **3. Build** ✅
- Creates distribution ZIP files
- Packages application artifacts

### **4. Publish** ✅
- Publishes `thecore-api` library to Artifactory
- Publishes `thecore-router` library to Artifactory
- Uses SSL truststore for secure connection
- **⚠️ Requires:** ARTIFACTORY_USER and ARTIFACTORY_PASSWORD in GitLab variables

### **5. Package** ✅
- Builds Docker image with Eclipse Temurin 11 JDK Alpine
- Tags image as: `thecore-router:4.0.0-RC-ff1eb48`
- Pushes to Docker registry
- Also tags as `:latest`

---

## ✨ Changes Included in This RC

### **CI/CD Pipeline Improvements:**
1. ✅ **Enabled test stage** - Mandatory unit tests on every pipeline run
2. ✅ **Maven publishing** - Both api and router libraries published to Artifactory
3. ✅ **Dynamic VERSION** - Branch-based versioning (QA, RC, production tags)
4. ✅ **Docker improvements** - Modern Eclipse Temurin base image, smart artifact detection
5. ✅ **SSL/TLS configuration** - Proper truststore for Artifactory connections
6. ✅ **Credential validation** - Checks Artifactory credentials before publishing

### **Build Configuration:**
1. ✅ **Java 11 consistency** - All stages use Java 11 (compile, test, build, Docker)
2. ✅ **Gradle publishing** - Proper Maven publication with sources JARs
3. ✅ **Artifactory integration** - Snapshot/release repository routing

### **Docker Configuration:**
1. ✅ **Modern base image** - Eclipse Temurin 11 JDK Alpine (actively maintained)
2. ✅ **Smart artifact detection** - Uses actual built artifact regardless of version
3. ✅ **Proper tagging** - Meaningful RC version tags

---

## 🧪 Testing This Release Candidate

### **Monitor Pipeline:**
```
https://gitlab.bayportfinance.com/nova/the-core-java/-/pipelines
```

### **Expected Pipeline Flow:**

```
┌─────────────┐
│  compile    │  ← Assembles code
└─────┬───────┘
      │
┌─────▼───────┐
│    test     │  ← Runs unit tests (mandatory)
└─────┬───────┘
      │
┌─────▼───────┐
│   build     │  ← Creates distribution artifacts
└─────┬───────┘
      │
┌─────▼───────┐
│  publish    │  ← Pushes to Artifactory (if credentials set)
└─────┬───────┘
      │
┌─────▼───────┐
│  package    │  ← Builds & pushes Docker image
└─────────────┘
```

### **Success Criteria:**

- ✅ All tests pass
- ✅ Build artifacts created
- ✅ Artifacts published to Artifactory (if credentials configured)
- ✅ Docker image built successfully
- ✅ Docker image pushed to registry

---

## ⚠️ Prerequisites

### **Required GitLab CI/CD Variables:**

These must be configured in: **Settings → CI/CD → Variables**

| Variable | Description | Required For | Masked? |
|----------|-------------|--------------|---------|
| `ARTIFACTORY_USER` | Artifactory username | Publish stage | No |
| `ARTIFACTORY_PASSWORD` | Artifactory password/token | Publish stage | **Yes** |
| `DOCKER_REGISTRY_USER` | Docker registry username | Package stage | No |
| `DOCKER_REGISTRY_PASS` | Docker registry password | Package stage | **Yes** |
| `BIGS_JAVA_CACERTS_PASS` | Java truststore password | SSL connections | **Yes** |

**If publish stage fails with 401:**
- Check that ARTIFACTORY_USER and ARTIFACTORY_PASSWORD are set
- Verify credentials are correct
- Check the before_script output for validation messages

---

## 🔍 Verification After Pipeline Completes

### **1. Check Artifactory:**
```bash
# Browse to:
https://artifactory.paas.bayportfinance.com/artifactory/webapp/#/artifacts/browse/tree/General/libs-snapshot-local/com/korwe

# Or via API:
curl -u ${ARTIFACTORY_USER}:${ARTIFACTORY_PASSWORD} \
  "https://artifactory.paas.bayportfinance.com/artifactory/api/storage/libs-snapshot-local/com/korwe/thecore-api/4.0.0-SNAPSHOT"
```

### **2. Check Docker Registry:**
```bash
# Verify image exists:
curl -u ${DOCKER_REGISTRY_USER}:${DOCKER_REGISTRY_PASS} \
  https://docker-registry-default.paas.bayportfinance.com/v2/novalend/thecore-router/tags/list

# Should show:
# - 4.0.0-RC-ff1eb48
# - latest
```

### **3. Pull and Test Docker Image:**
```bash
# Pull the RC image:
docker pull docker-registry-default.paas.bayportfinance.com/novalend/thecore-router:4.0.0-RC-ff1eb48

# Inspect the image:
docker inspect docker-registry-default.paas.bayportfinance.com/novalend/thecore-router:4.0.0-RC-ff1eb48

# Run the image (test locally):
docker run -it --rm \
  -e RABBITMQ_HOST=your-rabbitmq-host \
  docker-registry-default.paas.bayportfinance.com/novalend/thecore-router:4.0.0-RC-ff1eb48
```

---

## 📋 Next Steps

### **If RC Pipeline Succeeds:**

1. ✅ **Review pipeline output** - Check all stages completed successfully
2. ✅ **Verify artifacts** - Check Artifactory and Docker registry
3. ✅ **Test Docker image** - Deploy to test environment
4. ✅ **Validate functionality** - Run integration tests
5. ✅ **Create Merge Request** - Merge `release-4.0.0` → `main`

### **Creating Merge Request to Main:**

```bash
# Create MR via GitLab UI:
https://gitlab.bayportfinance.com/nova/the-core-java/-/merge_requests/new?merge_request[source_branch]=release-4.0.0

# Or via gh CLI:
gh pr create \
  --title "[NOVDEV-2114] Release 4.0.0 - CI/CD Pipeline Improvements" \
  --body "$(cat RELEASE-4.0.0-RC.md)" \
  --base main \
  --head release-4.0.0
```

### **MR Description Template:**

```markdown
## Release 4.0.0 - CI/CD Pipeline Improvements

**JIRA:** [NOVDEV-2114](https://jira.bayportfinance.com:8443/browse/NOVDEV-2114)
**Release Candidate:** Tested on `release-4.0.0` branch
**Docker Image:** `thecore-router:4.0.0-RC-ff1eb48`

### Summary
Complete CI/CD pipeline implementation enabling automated testing, artifact publishing, and Docker builds.

### Changes
- ✅ Enabled mandatory test stage
- ✅ Maven publishing to Artifactory (api + router)
- ✅ Docker build with Eclipse Temurin 11
- ✅ Dynamic VERSION for branch-based tagging
- ✅ SSL/TLS configuration for Artifactory
- ✅ Credential validation

### Testing
- ✅ QA branch: `qa/full-pipeline-test-20251028` - All stages passed
- ✅ Release branch: `release-4.0.0` - RC testing complete

### Verification
- ✅ All tests passing
- ✅ Artifacts published to Artifactory
- ✅ Docker image built and pushed
- ✅ Integration tests successful

### Documentation
See `BUILD-RELEASE-STRATEGY.md` for complete pipeline documentation.

### Reviewers
@team-lead @devops-team
```

---

## 🚀 After Merge to Main

Once merged to main, the pipeline will:
1. Run with VERSION: `4.0.0-SNAPSHOT`
2. Publish artifacts to Artifactory snapshot repository
3. Build Docker image: `thecore-router:4.0.0-SNAPSHOT`
4. Tag as `:latest`

### **Creating Production Release Tag:**

When ready for production release:
```bash
git checkout main
git pull origin main
git tag -a v4.0.0 -m "Release 4.0.0 - CI/CD Pipeline Improvements

Complete CI/CD implementation with automated testing,
artifact publishing, and Docker builds.

JIRA: NOVDEV-2114"
git push origin v4.0.0
```

This will trigger a production release pipeline with:
- VERSION: `v4.0.0-<sha>`
- Artifacts published to `libs-release-local` (not snapshot)
- Docker image: `thecore-router:v4.0.0-<sha>`

---

## 📚 Additional Documentation

- **Full pipeline strategy:** `BUILD-RELEASE-STRATEGY.md`
- **Getting started guide:** `GETTING-STARTED-PHASE1.md`
- **JIRA roadmap:** `JIRA-ROADMAP-MAPPING.md`
- **Team communication:** `SHARE-WITH-TEAM.md`

---

## 🎯 Summary

**Release:** 4.0.0 Release Candidate
**Branch:** `release-4.0.0`
**Status:** Pipeline running
**Monitor:** https://gitlab.bayportfinance.com/nova/the-core-java/-/pipelines
**Docker Image:** `thecore-router:4.0.0-RC-ff1eb48`

**Next:** Monitor pipeline → Verify artifacts → Test Docker image → Merge to main

---

**Questions?** Check the documentation files or contact the team lead.
