"use strict";
const assert = require('node:assert/strict');
const host = process.env.FIRESTORE_EMULATOR_HOST;
assert(host && /^(127\.0\.0\.1|localhost):\d+$/.test(host), 'Local emulator required');
const project = 'demo-astrosea-rules';
const root = `http://${host}/v1/projects/${project}/databases/(default)/documents`;
const token = uid => {
  const encode = v => Buffer.from(JSON.stringify(v)).toString('base64url');
  return `${encode({alg:'none',typ:'JWT'})}.${encode({sub:uid,user_id:uid,aud:project,iss:`https://securetoken.google.com/${project}`,iat:Math.floor(Date.now()/1000),exp:Math.floor(Date.now()/1000)+3600,firebase:{sign_in_provider:'password'}})}.`;
};
async function request(method,path,fields,uid='alice',admin=false) {
  const response = await fetch(root+'/'+path,{method,headers:{'Content-Type':'application/json',...(uid?{Authorization:'Bearer '+(admin?'owner':token(uid))}:{})},...(fields?{body:JSON.stringify({fields})}:{})});
  await response.text(); return response.status;
}
const str = stringValue => ({stringValue});
const bool = booleanValue => ({booleanValue});
(async()=>{
  assert.equal(await request('PATCH','users/alice',{name:str('Alice'),isPremium:bool(false)}),200);
  assert.equal(await request('GET','users/alice'),200);
  assert.equal(await request('GET','users/alice',null,'bob'),403);
  assert.equal(await request('GET','users/alice',null,null),403);
  assert.equal(await request('PATCH','users/bob',{isPremium:bool(true)},'bob'),403);
  assert.equal(await request('PATCH','users/alice?updateMask.fieldPaths=name',{name:str('Updated')}),200);
  assert.equal(await request('PATCH','users/alice?updateMask.fieldPaths=isPremium',{isPremium:bool(true)}),403);
  assert.equal(await request('PATCH','users/alice?updateMask.fieldPaths=premiumEndDate',{premiumEndDate:str('2099')}),403);
  assert.equal(await request('PATCH','users/alice/private/membershipState',{isPremium:bool(true)}),403);
  assert.equal(await request('GET','users/alice/private/membershipState'),403);
  assert.equal(await request('PATCH','betaTesters/test',{enabled:bool(true)}),403);
  assert.equal(await request('GET','betaTesters/test'),403);
  assert.equal(await request('PATCH','users/alice/notifications/one',{message:str('test')}),200);
  assert.equal(await request('DELETE','users/alice'),403);
  assert.equal(await request('PATCH','users/alice?updateMask.fieldPaths=membershipDeletionPending',{membershipDeletionPending:bool(true)},'admin',true),200);
  assert.equal(await request('PATCH','users/alice?updateMask.fieldPaths=membershipDeletionPending',{membershipDeletionPending:bool(false)}),403);
  assert.equal(await request('PATCH','users/alice?updateMask.fieldPaths=name',{name:str('Blocked')}),403);
  assert.equal(await request('PATCH','users/alice/notifications/two',{message:str('blocked')}),403);
  assert.equal(await request('DELETE','users/alice',null,'admin',true),200);
  console.log('PASS 19 Firestore rules checks (local emulator only)');
})().catch(error=>{console.error(error.message);process.exitCode=1;});
