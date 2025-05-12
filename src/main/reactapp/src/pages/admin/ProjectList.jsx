// =======================================================================================
// ProjectList.jsx | rw 25-05-11 최종 안정화 (관리자 전용 프로젝트 목록)
// =======================================================================================

import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getProjectList } from '../../api/projectApi';
import {
    Typography, Grid, Card, Box, Divider, Button
} from '@mui/joy';

export default function ProjectList() {
    const [list, setList] = useState([]);
    const navigate = useNavigate();
    const token = localStorage.getItem('token');

    useEffect(() => {
        const fetch = async () => {
            try {
                const res = await getProjectList(token);
                setList(res.data || []);
            } catch {
                alert('프로젝트 목록 조회 실패');
            }
        };
        fetch();
    }, [token]);

    return (
        <Box sx={{ px: 3, py: 3, bgcolor: '#fff' }}>
            <Typography level="h3" sx={{ mb: 3, fontWeight: 'bold', color: '#12b886' }}>
                📁 프로젝트 목록
            </Typography>

            <Grid container spacing={2}>
                {list.map((p) => (
                    <Grid key={p.pno} xs={12} sm={6} md={4}>
                        <Card variant="outlined" sx={{ bgcolor: '#f8f9fa', p: 2 }}>
                            <Typography level="title-md" sx={{ fontWeight: 'bold', color: '#12b886' }}>
                                {p.pname || '제목 없음'}
                            </Typography>
                            <Divider sx={{ my: 1 }} />
                            <Box sx={{ fontSize: 14 }}>
                                <p><strong>번호:</strong> {p.pno}</p>
                                <p><strong>모집인원:</strong> {p.pcount ?? '-'}명</p>
                                <p><strong>시작일:</strong> {p.pstart?.substring(0, 10) || '-'}</p>
                            </Box>
                            <Button
                                onClick={() => navigate(`/admin/project/${p.pno}`)}
                                variant="outlined"
                                sx={{ mt: 2, borderColor: '#12b886', color: '#12b886' }}
                            >
                                상세보기
                            </Button>
                        </Card>
                    </Grid>
                ))}
            </Grid>
        </Box>
    );
}