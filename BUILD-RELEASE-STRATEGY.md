# Build & Release Strategy - The Core Java

**Date:** 2025-10-28
**Purpose:** Complete build, test, and release pipeline strategy
**Status:** Active - Ready for implementation

---

## 🎯 Current State Analysis

### **Current Pipeline Stages**
Your `.gitlab-ci.yml` already has these stages:
1. **compile** - Assemble code and test classes
2. **test** - Run unit tests (we just enabled this!)
3. **build** - Create distribution artifacts
4. **package** - Build Docker images
5. **startup** - (appears to be placeholder)

### **Current Version Strategy**
Looking at lines 19-33 of `.gitlab-ci.yml`, you have rules that set VERSION based on branch:

```yaml
- if: $CI_COMMIT_TAG
  variables:
    VERSION: "${CI_COMMIT_REF_NAME}-${CI_COMMIT_SHA:0:7}"

- if: $CI_COMMIT_REF_NAME =~ /^release-.*/
  variables:
    VERSION: "${CI_COMMIT_REF_NAME:8}-RC-${CI_COMMIT_SHA:0:7}"

- if: $CI_COMMIT_REF_NAME =~ /^qa\/.*/
  variables:
    VERSION: "${CI_COMMIT_REF_NAME:3}-QA-${CI_COMMIT_SHA:0:7}"
```

**This is GOOD!** You already have branch-based versioning patterns!

---

## 🚀 Proposed Full Build & Release Strategy

### **Branch Types & Build Behavior**

| Branch Pattern | Version Format | Docker Built? | Deployed Where? | Purpose |
|----------------|---------------|---------------|-----------------|---------|
| `feat/*` | 4.0.0-SNAPSHOT | ❌ No | Nowhere | Feature development |
| `bugfix/*` | 4.0.0-SNAPSHOT | ❌ No | Nowhere | Bug fixes |
| `qa/*` | `{name}-QA-{sha}` | ✅ Yes | QA environment | QA testing |
| `release-*` | `{ver}-RC-{sha}` | ✅ Yes | Staging | Release candidate |
| `main` | 4.0.0-SNAPSHOT | ✅ Yes | Dev environment | Main development |
| **git tag** `v*` | Tag name (e.g., v4.0.1) | ✅ Yes | Production | Production release |

---

## 🧪 How to Test Full Pipeline End-to-End

### **Option 1: QA Branch (RECOMMENDED for testing)**

**Perfect for testing full pipeline without affecting anything!**

```bash
# Create QA test branch
git checkout -b qa/nightly-2025-10-28

# Make any test changes (or no changes, just trigger pipeline)
git commit --allow-empty -m "qa: Nightly build test $(date +%Y-%m-%d)"

# Push - triggers FULL pipeline including Docker build
git push origin qa/nightly-2025-10-28
```

**What happens:**
1. ✅ Compile stage runs
2. ✅ Test stage runs (new!)
3. ✅ Build stage runs
4. ✅ **Docker image built** with version: `nightly-2025-10-28-QA-{sha}`
5. ✅ **Pushed to registry**: `docker-registry-default.paas.bayportfinance.com/novalend/thecore-router:nightly-2025-10-28-QA-{sha}`

**Benefits:**
- ✅ Full end-to-end pipeline test
- ✅ Real Docker image created
- ✅ Can deploy to QA environment
- ✅ Doesn't affect main or production
- ✅ Can delete branch when done

---

### **Option 2: Release Candidate Branch**

**For testing release process:**

```bash
# Create release candidate branch
git checkout -b release-4.0.0

# Push - triggers full pipeline with RC versioning
git push origin release-4.0.0
```

**What happens:**
- ✅ Version becomes: `4.0.0-RC-{sha}`
- ✅ Full pipeline runs (compile → test → build → package)
- ✅ Docker image: `thecore-router:4.0.0-RC-{sha}`
- ✅ Ready for staging deployment

---

### **Option 3: Nightly/Daily Build Pattern**

**Set up scheduled pipeline for daily builds:**

Add to `.gitlab-ci.yml`:

```yaml
# Add this to your existing pipeline
nightly-build:
  stage: package
  rules:
    - if: $CI_PIPELINE_SOURCE == "schedule"
      variables:
        VERSION: "nightly-${CI_COMMIT_SHORT_SHA}"
  script:
    - echo "Running nightly build with version ${VERSION}"
  # ... rest of Docker build

# Or trigger via API/schedule
```

**Configure in GitLab:**
1. Go to: CI/CD → Schedules
2. Create schedule: "Nightly Build"
3. Interval: Daily at 2 AM
4. Target branch: `main` or `develop`
5. Variables: `BUILD_TYPE=nightly`

---

## 🔄 Complete Release Workflow (GitFlow-based)

### **Development Flow**

```
1. Feature Branch (feat/NOVDEV-2114-enable-ci-tests)
   ↓ (compile → test → build)
   ↓ (MR + approval)

2. Main Branch (main)
   ↓ (compile → test → build → Docker SNAPSHOT)
   ↓ (ready for dev deployment)

3. QA Branch (qa/sprint-23)
   ↓ (full pipeline + Docker QA build)
   ↓ (deploy to QA, test)

4. Release Branch (release-4.1.0)
   ↓ (full pipeline + Docker RC build)
   ↓ (deploy to staging, final testing)

5. Git Tag (v4.1.0)
   ↓ (full pipeline + Docker RELEASE build)
   ↓ (deploy to production)
```

---

## 📋 Detailed: Testing Full Pipeline TODAY

### **Step 1: Create QA Branch to Test Everything**

```bash
# You're currently on: feat/NOVDEV-2114-enable-ci-tests
# Let's create a QA branch to test FULL pipeline

git checkout main
git pull origin main

# Create QA test branch
git checkout -b qa/pipeline-test-$(date +%Y%m%d)

# Create empty commit to trigger pipeline
git commit --allow-empty -m "[NOVDEV-2114] qa: Test full CI/CD pipeline

Testing complete end-to-end pipeline including Docker build.

Stages to validate:
- compile: Assemble code
- test: Run unit tests (newly enabled)
- build: Create distribution artifacts
- package: Build and push Docker image

Expected Docker image:
docker-registry-default.paas.bayportfinance.com/novalend/thecore-router:pipeline-test-$(date +%Y%m%d)-QA-{sha}"

# Push QA branch
git push origin qa/pipeline-test-$(date +%Y%m%d)
```

**This will trigger THE FULL PIPELINE** including:
- ✅ Compile
- ✅ Test (with our new changes!)
- ✅ Build
- ✅ **Docker image build and push**

---

### **Step 2: Monitor the Pipeline**

**Watch it run:**
```
https://gitlab.bayportfinance.com/nova/the-core-java/-/pipelines
```

**You'll see:**
1. **compile-app-gradle** - Compiles code ✅
2. **test-app-gradle** - Runs tests (NEW!) ✅
3. **build-app-gradle** - Builds artifacts ✅
4. **build-docker-gradle-core-java** - **Builds Docker image!** 🐳

---

### **Step 3: Verify Docker Image**

After pipeline completes, verify Docker image exists:

```bash
# Check registry (if you have access)
curl -u ${DOCKER_REGISTRY_USER}:${DOCKER_REGISTRY_PASS} \
  https://docker-registry-default.paas.bayportfinance.com/v2/novalend/thecore-router/tags/list

# Or check in OpenShift/Kubernetes
oc get imagestreams -n novalend | grep thecore-router
```

---

## 🎯 Recommended: Daily/Nightly Build Setup

### **Create Nightly QA Build**

**Option A: Scheduled Pipeline**

Add schedule in GitLab:
1. Go to: **CI/CD → Schedules**
2. Click **New schedule**
3. Configure:
   - Description: `Nightly QA Build`
   - Interval: `0 2 * * *` (2 AM daily)
   - Target branch: `main`
   - Variables:
     - `BUILD_TYPE=nightly`
     - `VERSION=nightly-$(date +%Y%m%d)`

**Option B: QA Branch Pattern**

```bash
# Daily script to create QA build
#!/bin/bash
DATE=$(date +%Y%m%d)
BRANCH="qa/nightly-${DATE}"

git checkout main
git pull origin main
git checkout -b ${BRANCH}
git commit --allow-empty -m "qa: Nightly build ${DATE}"
git push origin ${BRANCH}

# Optionally: delete old qa/nightly-* branches after 7 days
```

---

## 🔧 Improved Pipeline Configuration

Here's an enhanced `.gitlab-ci.yml` section for better version control:

```yaml
variables:
  # Default version
  VERSION: 4.0.0-SNAPSHOT

  # Docker registry config
  DOCK_REGISTRY: "docker-registry-default.paas.bayportfinance.com"
  DOCK_REGISTRY_PATH: "$DOCK_REGISTRY/novalend"

.version_rules: &version_rules
  rules:
    # Production release (git tag)
    - if: $CI_COMMIT_TAG =~ /^v.*/
      variables:
        VERSION: $CI_COMMIT_TAG
        DEPLOY_ENV: "production"

    # Release candidate (release-* branches)
    - if: $CI_COMMIT_REF_NAME =~ /^release-.*/
      variables:
        VERSION: "${CI_COMMIT_REF_NAME:8}-RC-${CI_COMMIT_SHORT_SHA}"
        DEPLOY_ENV: "staging"

    # QA builds (qa/* branches)
    - if: $CI_COMMIT_REF_NAME =~ /^qa\/.*/
      variables:
        VERSION: "${CI_COMMIT_REF_NAME:3}-QA-${CI_COMMIT_SHORT_SHA}"
        DEPLOY_ENV: "qa"

    # Nightly builds (scheduled)
    - if: $CI_PIPELINE_SOURCE == "schedule"
      variables:
        VERSION: "nightly-${CI_COMMIT_SHORT_SHA}"
        DEPLOY_ENV: "dev"

    # Main/develop branch
    - if: $CI_COMMIT_REF_NAME == "main"
      variables:
        VERSION: "4.0.0-SNAPSHOT"
        DEPLOY_ENV: "dev"

    # Feature branches (no Docker build)
    - if: $CI_COMMIT_REF_NAME =~ /^feat\/.*/
      when: never

    # Default
    - when: always
```

---

## 📊 Pipeline Stages Explained

### **Stage 1: Compile** (Always runs)
```yaml
compile-app-gradle:
  stage: compile
  script:
    - ./gradlew assemble testClasses
  artifacts:
    paths:
      - router/build/distributions/*.zip
```
**Purpose:** Compile code, prepare for testing

---

### **Stage 2: Test** (Always runs - NEW!)
```yaml
test-app-gradle:
  stage: test
  script:
    - ./gradlew test
  needs: ["compile-app-gradle"]
  allow_failure: false  # MUST pass!
```
**Purpose:** Run unit tests, fail fast if broken

---

### **Stage 3: Build** (Always runs)
```yaml
build-app-gradle:
  stage: build
  script:
    - ./gradlew build
  artifacts:
    paths:
      - router/build/distributions/*.zip
```
**Purpose:** Create distribution artifacts

---

### **Stage 4: Package** (Only for qa/*, release-*, tags)
```yaml
build-docker-gradle-core-java:
  stage: package
  rules:
    - if: $CI_COMMIT_TAG
    - if: $CI_COMMIT_REF_NAME =~ /^release-.*/
    - if: $CI_COMMIT_REF_NAME =~ /^qa\/.*/
  script:
    - docker build -t thecore-router:${VERSION}
    - docker push ${DOCK_REGISTRY_PATH}/thecore-router:${VERSION}
```
**Purpose:** Build and publish Docker images

---

## 🎯 Action Plan: Test Full Pipeline NOW

### **Immediate Next Steps (Next 30 minutes)**

```bash
# 1. Create QA test branch
git checkout main
git pull origin main
git checkout -b qa/full-pipeline-test

# 2. Trigger pipeline
git commit --allow-empty -m "[NOVDEV-2114] qa: Test complete CI/CD pipeline"
git push origin qa/full-pipeline-test

# 3. Watch pipeline run
# Open: https://gitlab.bayportfinance.com/nova/the-core-java/-/pipelines

# 4. Verify all stages pass:
#    ✅ compile
#    ✅ test (NEW!)
#    ✅ build
#    ✅ package (Docker image)

# 5. Check Docker image exists in registry

# 6. Clean up test branch (optional)
git push origin --delete qa/full-pipeline-test
```

---

## 📈 Future Enhancements (Phase 3)

These will come in Phase 3 of the roadmap:

1. **Semantic Versioning Automation**
   - Auto-increment version based on commit messages
   - Conventional commits (feat:, fix:, BREAKING CHANGE:)

2. **GitFlow Full Implementation**
   - `develop` branch for integration
   - `main` branch for production-ready code
   - Automated merge from release branches

3. **Automated Release Notes**
   - Generate CHANGELOG.md from commits
   - GitLab release pages with artifacts

4. **Deployment Automation**
   - Auto-deploy QA builds to QA environment
   - Manual approval for staging/production
   - Blue-green deployment support

---

## ✅ Summary

**To test full pipeline end-to-end RIGHT NOW:**

```bash
git checkout main
git checkout -b qa/pipeline-test
git commit --allow-empty -m "qa: Full pipeline test"
git push origin qa/pipeline-test
```

**Watch:** `https://gitlab.bayportfinance.com/nova/the-core-java/-/pipelines`

**You'll see:**
- ✅ Compile → Test → Build → **Docker Package**
- ✅ Docker image: `thecore-router:pipeline-test-QA-{sha}`
- ✅ Full end-to-end validation

**No risk to main or production!** 🔒

---

**Questions?** This document covers:
- ✅ Current pipeline analysis
- ✅ Full build & release strategy
- ✅ How to test end-to-end TODAY
- ✅ Nightly/daily build setup
- ✅ Future enhancements (Phase 3)

Ready to run the full pipeline test? 🚀
