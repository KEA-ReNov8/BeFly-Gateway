module.exports = {
    parserPreset: {
        parserOpts: {
            headerPattern: /^(:\w+: )?(\w+): \[KAN-\d+\] (.+)$/,
            headerCorrespondence: ['emoji', 'type', 'subject'],
        },
    },
    rules: {
        'type-enum': [2, 'always', ['feat', 'fix', 'refactor', 'test', 'chore', 'docs']],
        'subject-empty': [2, 'never'],
        'type-empty': [2, 'never'],
        'header-max-length': [2, 'always', 100],
    },
};
